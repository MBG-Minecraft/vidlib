package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.klib.texture.UV;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Quaternionf;

public class PhysicsParticleRenderState {
	public double x;
	public double y;
	public double z;
	public float scale;
	public Quaternionf rotation = new Quaternionf();
	public PhysicsParticleShape shape;
	public UV baseUV;
	public int tint;
	public int light = LightCoordsUtil.FULL_BRIGHT;
}
