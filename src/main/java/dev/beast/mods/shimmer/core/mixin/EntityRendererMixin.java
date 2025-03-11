package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean shimmer$shouldShowName(boolean original, @Local(argsOnly = true) Entity entity) {
		if (!original && entity instanceof LocalPlayer && Minecraft.getInstance().isLocalServer() && !Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			return true;
		}

		return original;
	}
}
