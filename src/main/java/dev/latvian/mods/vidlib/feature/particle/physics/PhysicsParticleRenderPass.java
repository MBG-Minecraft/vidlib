package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.vidlib.feature.client.StaticBuffers;

public class PhysicsParticleRenderPass {
	public final RenderPass renderPass;
	public StaticBuffers lastBuffers;

	PhysicsParticleRenderPass(RenderPass renderPass) {
		this.renderPass = renderPass;
		this.lastBuffers = null;
	}
}
