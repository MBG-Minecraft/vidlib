package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.skin.VLSkin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
		at = @At("RETURN")
	)
	private void video$extractRenderState(AbstractClientPlayer player, PlayerRenderState state, float delta, CallbackInfo ci) {
		VLSkin.modifyPlayerRenderState(player, state, delta);
	}

}
