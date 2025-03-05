package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.structure.StructureRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;", ordinal = 0))
	private Entity shimmer$getFocusedEntity0(Camera camera) {
		return minecraft.screen != null && minecraft.screen.renderPlayer() ? null : camera.getEntity();
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;", ordinal = 3))
	private Entity shimmer$getFocusedEntity3(Camera camera) {
		return minecraft.screen != null && minecraft.screen.renderPlayer() ? minecraft.player : camera.getEntity();
	}

	/**
	 * Cancel sound that plays when you switch dimensions
	 */
	@Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
	private void shimmer$levelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
		if (eventId == 1032) {
			ci.cancel();
		}
	}

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void shimmer$allChanged(CallbackInfo ci) {
		StructureRenderer.redrawAll();
	}
}
