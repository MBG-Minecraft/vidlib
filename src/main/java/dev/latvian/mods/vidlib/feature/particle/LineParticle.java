package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class LineParticle extends CustomParticle {
	private final Gradient startColor;
	private final Gradient endColor;
	private final int endOffset;
	private final Vec3f vector;

	protected LineParticle(LineParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.startColor = options.startColor().resolve();
		this.endColor = options.endColor().resolve();
		this.endOffset = options.endOffset();
		setLifetime(options.lifespan());
		vector = Vec3f.of(vx, vy, vz);
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		var cameraPos = camera.getPosition();
		var rx = (float) (KMath.lerp(delta, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(delta, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(delta, zo, z) - cameraPos.z);

		var m = ms.last().pose();
		var buffer = buffers.getBuffer(DebugRenderTypes.LINES);
		var sc = startColor.get(time / (float) lifetime).fadeOut(time, lifetime, 20F);
		float etime = Math.clamp(time + endOffset, 0F, lifetime);
		var ec = endColor.get(etime / (float) lifetime).fadeOut(etime, lifetime, 20F);

		var nv = vector.normalize();

		buffer.addVertex(m, rx, ry, rz).setColor(sc.argb()).setNormal(nv.x(), nv.y(), nv.z());
		buffer.addVertex(m, rx + vector.x(), ry + vector.y(), rz + vector.z()).setColor(ec.argb()).setNormal(nv.x(), nv.y(), nv.z());
	}
}
