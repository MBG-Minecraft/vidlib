package dev.latvian.mods.vidlib.feature.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.auto.ScannedAnnotation;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.capture.PacketCaptureEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.nio.file.Path;
import java.util.Set;
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
	public Path findFile(String modid, String... path) {
		return ModList.get().getModFileById(modid).getFile().findResource(path);
	}

	@Override
	public Path findVidLibFile(String... path) {
		return mod.getModInfo().getOwningFile().getFile().findResource(path);
	}
}
