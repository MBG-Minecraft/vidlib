package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLOutlineBufferSource;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
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
		var override = ClientGameEngine.INSTANCE.overrideRenderType(renderType, vl$isPlayer);

		if (override != null) {
			return (T) override;
		}

		return instance.get();
	}
}
