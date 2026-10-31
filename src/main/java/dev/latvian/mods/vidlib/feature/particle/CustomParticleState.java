package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface CustomParticleState {
	void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState);
}
