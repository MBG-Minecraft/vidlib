package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class LineParticle extends CustomParticle {
	private final Ref<Gradient> startColor;
	private final Ref<Gradient> endColor;
	private final int endOffset;
	private final Vec3f vector;

	protected LineParticle(LineParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.startColor = options.startColor();
		this.endColor = options.endColor();
		this.endOffset = options.endOffset();
		setLifetime(options.lifespan());
		vector = Vec3f.of(vx, vy, vz);
	}

	@Override
	public void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		float time = KMath.lerp(delta, prevAge, age);

		var cameraPos = camera.position();
		var rx = (float) (KMath.lerp(delta, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(delta, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(delta, zo, z) - cameraPos.z);

		var m = ms.last();
		var buffer = buffers.getBuffer(DebugRenderTypes.LINES);
		var sc = startColor.value().get(time / (float) lifetime).fadeOut(time, lifetime, 20F);
		float etime = Math.clamp(time + endOffset, 0F, lifetime);
		var ec = endColor.value().get(etime / (float) lifetime).fadeOut(etime, lifetime, 20F);

		var nv = vector.normalize();
		var posed = m.transform(buffer);
		posed.withColor(sc).withNormal(nv.x(), nv.y(), nv.z()).acceptPos(rx, ry, rz);
		posed.withColor(ec).withNormal(nv.x(), nv.y(), nv.z()).acceptPos(rx + vector.x(), ry + vector.y(), rz + vector.z());
	}
}
