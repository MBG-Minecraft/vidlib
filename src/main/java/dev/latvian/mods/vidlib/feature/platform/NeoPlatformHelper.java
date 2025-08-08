package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.auto.ScannedAnnotation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Set;

public class NeoPlatformHelper extends PlatformHelper {
	@Override
	public Side getSide() {
		return FMLLoader.getDist().isClient() ? Side.CLIENT : Side.SERVER;
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
							} catch (Exception ex) {
								throw new RuntimeException("Failed to process @" + annotation.getSimpleName() + " for '" + mod.getDisplayName() + "' mod", ex);
							}
						}
					}
				}
			}
		}
	}
}
