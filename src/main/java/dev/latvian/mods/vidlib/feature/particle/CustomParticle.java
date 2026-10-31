package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class CustomParticle extends Particle {
	public CustomParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z, vx, vy, vz);
		setSize(1F, 1F);
	}

	public CustomParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		setSize(1F, 1F);
	}

	@Override
	public ParticleRenderType getGroup() {
		return VidLibClientParticles.CUSTOM_RENDER_TYPE;
	}

	@Nullable
	public abstract CustomParticleState extract(Frustum frustum, Camera camera, Vec3 cameraPos, float delta);
}
