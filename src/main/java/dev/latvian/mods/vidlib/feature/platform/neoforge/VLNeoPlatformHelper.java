package dev.latvian.mods.vidlib.feature.platform.neoforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.auto.ScannedAnnotation;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterTypeRegistryEvent;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationRegistryEvent;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeTypeRegistryEvent;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.capture.PacketCaptureEvent;
import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResourceEvent;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterRegistryEvent;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumberRegistryEvent;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconRegistryEvent;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRegistryEvent;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeRegistryEvent;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberRegistryEvent;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorRegistryEvent;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEvent;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEventRegistryEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforge.common.NeoForge;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class VLNeoPlatformHelper extends VLPlatformHelper {
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
									if (file instanceof ModFile modFile) {
										classLoader = FMLLoader.getCurrent().getGameLayer().findLoader(modFile.getModuleDescriptor().name());
									} else {
										classLoader = Thread.currentThread().getContextClassLoader();
									}
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
	public void collectDynamicResources(PackType type, Consumer<Identifier> callback) {
		ModLoader.postEvent(type == PackType.CLIENT_RESOURCES ? new DynamicResourceEvent.Assets(callback) : new DynamicResourceEvent.Data(callback));
	}

	@Override
	public void collectKNumbers(CustomRegistryTypeCollector<KNumber> registry) {
		super.collectKNumbers(registry);
		ModLoader.postEvent(new KNumberRegistryEvent(registry));
	}

	@Override
	public void collectKVectors(CustomRegistryTypeCollector<KVector> registry) {
		super.collectKVectors(registry);
		ModLoader.postEvent(new KVectorRegistryEvent(registry));
	}

	@Override
	public void collectEntityFilters(CustomRegistryTypeCollector<EntityFilter> registry) {
		super.collectEntityFilters(registry);
		ModLoader.postEvent(new EntityFilterRegistryEvent(registry));
	}

	@Override
	public void collectBlockFilters(CustomRegistryTypeCollector<BlockFilter> registry) {
		super.collectBlockFilters(registry);
		ModLoader.postEvent(new BlockFilterTypeRegistryEvent(registry));
	}

	@Override
	public void collectZoneShapes(CustomRegistryTypeCollector<ZoneShape> registry) {
		super.collectZoneShapes(registry);
		ModLoader.postEvent(new ZoneShapeRegistryEvent(registry));
	}

	@Override
	public void collectIcons(CustomRegistryTypeCollector<Icon> registry) {
		super.collectIcons(registry);
		ModLoader.postEvent(new IconRegistryEvent(registry));
	}

	@Override
	public void collectScreenShakeTypes(CustomRegistryTypeCollector<ScreenShakeType> registry) {
		super.collectScreenShakeTypes(registry);
		ModLoader.postEvent(new ScreenShakeTypeRegistryEvent(registry));
	}

	@Override
	public void collectBulkLevelModifications(CustomRegistryTypeCollector<BulkLevelModification> registry) {
		super.collectBulkLevelModifications(registry);
		ModLoader.postEvent(new BulkLevelModificationRegistryEvent(registry));
	}

	@Override
	public void collectScreenEffects(CustomRegistryTypeCollector<ScreenEffect> registry) {
		super.collectScreenEffects(registry);
		ModLoader.postEvent(new ScreenEffectRegistryEvent(registry));
	}

	@Override
	public void collectEntityNumbers(CustomRegistryTypeCollector<EntityNumber> registry) {
		super.collectEntityNumbers(registry);
		ModLoader.postEvent(new EntityNumberRegistryEvent(registry));
	}

	@Override
	public boolean isStaff(Entity entity) {
		return entity.isStaff();
	}

	@Override
	public boolean isStaffOrTalent(Entity entity) {
		return entity.isStaffOrTalent();
	}

	@Override
	public void collectGatewayEventHandlers(Map<String, Consumer<HubGatewayEvent>> map) {
		NeoForge.EVENT_BUS.post(new HubGatewayEventRegistryEvent(map));
	}
}
