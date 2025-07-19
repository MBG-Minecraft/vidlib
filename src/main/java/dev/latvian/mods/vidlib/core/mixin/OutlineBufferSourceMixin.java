package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.core.VLOutlineBufferSource;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(OutlineBufferSource.class)
public class OutlineBufferSourceMixin implements VLOutlineBufferSource {
	@Unique
	private boolean vl$isPlayer = false;

	@Override
	public void vl$setPlayer(boolean player) {
		vl$isPlayer = player;
	}

	@Redirect(method = "getBuffer", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;"))
	private <T> T vl$getBuffer(Optional<T> instance, @Local(argsOnly = true) RenderType renderType) {
		if (vl$isPlayer || VidLibConfig.strongEntityOutline) {
			var tex = renderType.vl$getTexture();

			if (tex != null) {
				Canvas.STRONG_OUTLINE.markActive();
				return (T) VidLibRenderTypes.STRONG_OUTLINE_NO_CULL.apply(tex);
			}
		}

		Canvas.WEAK_OUTLINE.markActive();
		return instance.get();
	}
}
