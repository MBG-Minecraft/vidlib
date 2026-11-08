package dev.latvian.mods.vidlib.feature.particle.physics;

import org.joml.Quaternionf;

public class PhysicsParticleRenderState {
	public double x;
	public double y;
	public double z;
	public float scale;
	public Quaternionf rotation = new Quaternionf();
	public PhysicsParticleShape shape;
	public int red, green, blue, alpha;
	public int lightU;
	public int lightV;
}
