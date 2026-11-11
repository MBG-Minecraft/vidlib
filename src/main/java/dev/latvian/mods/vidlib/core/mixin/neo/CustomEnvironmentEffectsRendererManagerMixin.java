package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomEnvironmentEffectsRendererManager;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomEnvironmentEffectsRendererManager.class)
public class CustomEnvironmentEffectsRendererManagerMixin {
	@Shadow
	public static void init() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@Inject(method = "getCustomSkyboxRenderer(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)Lnet/neoforged/neoforge/client/CustomSkyboxRenderer;", at = @At("HEAD"), cancellable = true)
	private static void vl$getCustomSkyboxRenderer(Level level, Vec3 position, CallbackInfoReturnable<CustomSkyboxRenderer> cir) {
		if (level instanceof ClientLevel l) {
			var session = ClientLevelSessionData.of(l);
			var atmosphere = session.getAtmosphere();

			if (atmosphere != null && atmosphere.value().skybox().isPresent()) {
				cir.setReturnValue(session.globalData.getAtmosphere(atmosphere));
			}
		}
	}
}
