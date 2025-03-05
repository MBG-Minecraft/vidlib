package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.misc.PlumbobRenderLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void shimmer$init(CallbackInfo ci) {
		var r = (PlayerRenderer) (Object) this;
		r.addLayer(new PlumbobRenderLayer(r));
	}

	@Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
	private void shimmer$skipRenderingIfCreativeInvisible(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		if (player.isCreative() && player.isInvisible()) {
			ci.cancel();
		}

		if (player.isSpectator() && !MiscShimmerClientUtils.canSeeSpectators(player)) {
			ci.cancel();
		}
	}

	@Redirect(method = "setModelProperties", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isSpectator()Z"))
	private boolean clutter$renderSpectators(AbstractClientPlayer instance) {
		if (MiscShimmerClientUtils.canSeeSpectators(instance)) {
			return false;
		}

		return instance.isSpectator();
	}
}
