package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.atmosphere.SkyboxData;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SkyRenderState.class)
public class SkyRenderStateMixin implements VLSkyRenderState {
	@Unique
	private boolean vl$sun = true;

	@Unique
	private boolean vl$moon = true;

	@Unique
	private Quaternionfc vl$celestialTilt = null;

	@Unique
	private SkyboxData vl$skyboxData = null;

	@Override
	public void vl$set(boolean sun, boolean moon, @Nullable Quaternionfc celestialTilt, @Nullable SkyboxData skyboxData) {
		vl$sun = sun;
		vl$moon = moon;
		vl$celestialTilt = celestialTilt;
		vl$skyboxData = skyboxData;
	}

	@Override
	public boolean vl$sun() {
		return vl$sun;
	}

	@Override
	public boolean vl$moon() {
		return vl$moon;
	}

	@Override
	@Nullable
	public Quaternionfc vl$celestialTilt() {
		return vl$celestialTilt;
	}

	@Override
	@Nullable
	public SkyboxData vl$skyboxTextureData() {
		return vl$skyboxData;
	}
}
