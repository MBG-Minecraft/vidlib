package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;

public class CustomParticle extends Particle {
	public int prevAge;

	public CustomParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z, vx, vy, vz);
		setSize(1F, 1F);
	}

	public CustomParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		setSize(1F, 1F);
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float delta) {
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}
}
