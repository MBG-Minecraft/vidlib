package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.Vec3f;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.render.DebugRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class LineParticle extends CustomParticle {
	private final LineParticleOptions options;
	private final Gradient startColor;
	private final Gradient endColor;
	private final Vec3f vector;

	protected LineParticle(LineParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		this.startColor = options.startColor().resolve();
		this.endColor = options.endColor().resolve();
		setLifetime(options.ttl());
		vector = Vec3f.of(vx, vy, vz);
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		var cameraPos = camera.getPosition();
		var rx = (float) (KMath.lerp(time, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(time, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(time, zo, z) - cameraPos.z);

		var m = ms.last().pose();
		var buffer = buffers.getBuffer(DebugRenderTypes.LINES);
		buffer.addVertex(m, rx, ry, rz).setColor(startColor.get(time / (float) lifetime).fadeOut(time, lifetime, 20F).argb());
		buffer.addVertex(m, rx + vector.x(), ry + vector.y(), rz + vector.z()).setColor(endColor.get(time / (float) lifetime).fadeOut(time, lifetime, 20F).argb());
	}
}
