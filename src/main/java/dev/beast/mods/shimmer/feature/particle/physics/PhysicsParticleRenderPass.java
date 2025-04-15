package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.systems.RenderPass;
import dev.beast.mods.shimmer.util.client.StaticBuffers;

public class PhysicsParticleRenderPass {
	public final RenderPass renderPass;
	public StaticBuffers lastBuffers;

	PhysicsParticleRenderPass(RenderPass renderPass) {
		this.renderPass = renderPass;
		this.lastBuffers = null;
	}
}
