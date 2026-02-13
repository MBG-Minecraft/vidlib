package dev.latvian.mods.vidlib.feature.platform.neoforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.auto.ScannedAnnotation;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterRegistryEvent;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationRegistryEvent;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeTypeRegistryEvent;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.capture.PacketCaptureEvent;
import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResourceEvent;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterRegistryEvent;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconRegistryEvent;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRegistryEvent;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeRegistryEvent;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberRegistryEvent;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorRegistryEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class NeoPlatformHelper extends PlatformHelper {
	public final ModContainer mod;

	public NeoPlatformHelper(ModContainer mod) {
		this.mod = mod;
	}

	@Override
	public Side getSide() {
		return FMLLoader.getDist().isClient() ? Side.CLIENT : Side.SERVER;
	}

	@Override
	public String getPlatform() {
		return "neoforge";
	}

	@Override
	public RegistryFriendlyByteBuf createBuffer(ByteBuf source, RegistryAccess access) {
		return new RegistryFriendlyByteBuf(source, access, ConnectionType.NEOFORGE);
	}

	@Override
	public RegistryFriendlyByteBuf createBuffer(ByteBuf source, RegistryFriendlyByteBuf parent) {
		return new RegistryFriendlyByteBuf(source, parent.registryAccess(), parent.getConnectionType());
	}

	@Override
	public Function<ByteBuf, RegistryFriendlyByteBuf> createDecorator(RegistryAccess access) {
		return RegistryFriendlyByteBuf.decorator(access, ConnectionType.NEOFORGE);
	}

	@Override
	public void load(Class<? extends Annotation> annotation, Set<ElementType> elementTypes, AutoCallback callback) {
		var annotationType = Type.getType(annotation);

		for (var mod : ModList.get().getMods()) {
			var owningFile = mod.getOwningFile();

			if (owningFile != null) {
				var file = owningFile.getFile();

				if (file != null) {
					ClassLoader classLoader = null;

					for (var ad : file.getScanResult().getAnnotations()) {
						if (elementTypes.contains(ad.targetType()) && ad.annotationType().equals(annotationType)) {
							try {
								if (classLoader == null) {
									classLoader = FMLLoader.getGameLayer().findLoader(owningFile.moduleName());
								}

								callback.accept(mod.getModId(), classLoader, new ScannedAnnotation(ad.annotationType(), ad.targetType(), ad.clazz(), ad.memberName(), ad.annotationData()));
							} catch (Throwable ex) {
								throw new RuntimeException("Failed to process @" + annotation.getSimpleName() + " on " + ad.clazz().getClassName() + " in '" + mod.getDisplayName() + "' mod", ex);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void finishPacketCapture(PacketCapture packetCapture) {
		NeoForge.EVENT_BUS.post(new PacketCaptureEvent.Finished(packetCapture));
	}

	@Override
	public void packetCaptureMetadata(PacketCapture packetCapture, JsonObject metadata) {
		NeoForge.EVENT_BUS.post(new PacketCaptureEvent.Metadata(packetCapture, metadata));

		var ml = new JsonArray();

		for (var mod : ModList.get().getMods()) {
			var json = new JsonObject();
			json.addProperty("id", mod.getModId());
			json.addProperty("name", mod.getDisplayName());
			json.addProperty("version", mod.getVersion().toString());
			ml.add(json);
		}

		metadata.add("mod_list", ml);
	}

	@Override
	@Nullable
	public Path findFile(String... path) {
		for (var file : ModList.get().getModFiles()) {
			var res = file.getFile().findResource(path);

			if (Files.exists(res)) {
				return res;
			}
		}

		return null;
	}

	@Override
	public void collectDynamicResources(PackType type, Consumer<ResourceLocation> callback) {
		ModLoader.postEvent(type == PackType.CLIENT_RESOURCES ? new DynamicResourceEvent.Assets(callback) : new DynamicResourceEvent.Data(callback));
	}

	@Override
	public void collectKNumbers(SimpleRegistryCollector<KNumber> registry) {
		super.collectKNumbers(registry);
		ModLoader.postEvent(new KNumberRegistryEvent(registry));
	}

	@Override
	public void collectKVectors(SimpleRegistryCollector<KVector> registry) {
		super.collectKVectors(registry);
		ModLoader.postEvent(new KVectorRegistryEvent(registry));
	}

	@Override
	public void collectEntityFilters(SimpleRegistryCollector<EntityFilter> registry) {
		super.collectEntityFilters(registry);
		ModLoader.postEvent(new EntityFilterRegistryEvent(registry));
	}

	@Override
	public void collectBlockFilters(SimpleRegistryCollector<BlockFilter> registry) {
		super.collectBlockFilters(registry);
		ModLoader.postEvent(new BlockFilterRegistryEvent(registry));
	}

	@Override
	public void collectZoneShapes(SimpleRegistryCollector<ZoneShape> registry) {
		super.collectZoneShapes(registry);
		ModLoader.postEvent(new ZoneShapeRegistryEvent(registry));
	}

	@Override
	public void collectIcons(SimpleRegistryCollector<Icon> registry) {
		super.collectIcons(registry);
		ModLoader.postEvent(new IconRegistryEvent(registry));
	}

	@Override
	public void collectScreenShakeTypes(SimpleRegistryCollector<ScreenShakeType> registry) {
		super.collectScreenShakeTypes(registry);
		ModLoader.postEvent(new ScreenShakeTypeRegistryEvent(registry));
	}

	@Override
	public void collectBulkLevelModifications(SimpleRegistryCollector<BulkLevelModification> registry) {
		super.collectBulkLevelModifications(registry);
		ModLoader.postEvent(new BulkLevelModificationRegistryEvent(registry));
	}

	@Override
	public void collectScreenEffects(SimpleRegistryCollector<ScreenEffect> registry) {
		super.collectScreenEffects(registry);
		ModLoader.postEvent(new ScreenEffectRegistryEvent(registry));
	}
}
