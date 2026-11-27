package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLLevelRenderState;
import dev.latvian.mods.vidlib.feature.client.VLWorldRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderState.class)
public abstract class LevelRenderStateMixin implements VLLevelRenderState {
	@Unique
	private final VLWorldRenderState vl$state = new VLWorldRenderState();

	@Override
	public VLWorldRenderState vl$state() {
		return vl$state;
	}

	@Inject(method = "reset", at = @At("RETURN"))
	private void vl$reset(CallbackInfo ci) {
		vl$state.reset();
	}
}
