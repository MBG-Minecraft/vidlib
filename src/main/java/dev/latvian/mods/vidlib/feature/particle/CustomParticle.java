package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public abstract class CustomParticle extends Particle {
	public int prevAge;

	public CustomParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z, vx, vy, vz);
		setSize(1F, 1F);
	}

	public CustomParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		setSize(1F, 1F);
	}

	public abstract void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta);

	public AABB getRenderBoundingBox(float partialTicks) {
		return getBoundingBox();
	}

	public boolean shouldRender(Camera camera, Frustum frustum, float partialTicks) {
		return frustum == null || frustum.isVisible(getRenderBoundingBox(partialTicks));
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();
	}

	@Override
	public ParticleRenderType getGroup() {
		return VidLibParticleRenderTypes.CUSTOM;
	}
}
