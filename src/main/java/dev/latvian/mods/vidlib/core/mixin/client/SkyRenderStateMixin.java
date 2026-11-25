package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.atmosphere.VLExtraSkyRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SkyRenderState.class)
public class SkyRenderStateMixin implements VLSkyRenderState {
	@Unique
	private final VLExtraSkyRenderState vl$extra = new VLExtraSkyRenderState();

	@Override
	public VLExtraSkyRenderState vl$extra() {
		return vl$extra;
	}
}
