package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.klib.vertex.VertexCallback;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Vector3d;

public class LightningParticle extends CustomParticle {
	private final LightningParticleOptions options;
	public final Rotation direction;
	public float prevRadiusMod;
	public float radiusMod;
	public final float length;
	public final float[] prevAngles;
	public final float[] prevDist;
	public final float[] angles;
	public final float[] dist;
	public final Gradient color;
	public final Gradient outlineColor;
	public final Vector3d target;
	private final Matrix3d matrix;

	protected LightningParticle(LightningParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.xd = 0D;
		this.yd = 0D;
		this.zd = 0D;
		this.direction = Rotation.compute(Vec3.ZERO, new Vec3(vx, vy, vz));
		this.options = options;
		this.length = (float) Mth.length(vx, vy, vz);
		int segments = Math.max(1, options.segments());
		this.prevAngles = new float[segments - 1];
		this.prevDist = new float[segments - 1];
		this.angles = new float[segments - 1];
		this.dist = new float[segments - 1];
		setLifetime(Math.abs(options.lifespan()));
		this.gravity = 0F;

		for (int i = 0; i < angles.length; i++) {
			angles[i] = random.nextFloat() * 360F;
			dist[i] = random.nextFloat() * options.spread();
		}

		this.color = options.color().optimize();
		this.outlineColor = options.outlineColor().optimize();
		this.target = new Vector3d();
		this.matrix = new Matrix3d();
		this.prevRadiusMod = radiusMod = 0F;

		// level.addParticle(new LineParticleOptions(options.lifespan(), Color.BLACK), x, y, z, vx, vy, vz);
	}

	@Override
	public void tick() {
		prevRadiusMod = radiusMod;

		for (int i = 0; i < angles.length; i++) {
			prevAngles[i] = angles[i];
			prevDist[i] = dist[i];
		}

		super.tick();

		for (int i = 0; i < angles.length; i++) {
			angles[i] = random.nextFloat() * 360F;
			dist[i] = random.nextFloat() * options.spread();
		}

		radiusMod = age >= lifetime - 1 ? 0F : 1F;
	}

	private void renderSegments(PoseStack ms, Vector3d prevPoint, Vector3d point, float delta, VertexCallback buffer) {
		float rmod = KMath.lerp(delta, prevRadiusMod, radiusMod);

		for (int i = 0; i < angles.length; i++) {
			var startRadius = (i == 0 ? options.endingRadius() : options.radius()) * rmod;
			var endRadius = options.radius() * rmod;

			var angle = Math.toRadians(Mth.rotLerp(delta, prevAngles[i], angles[i]));
			var d = KMath.lerp(delta, prevDist[i], dist[i]) * options.spread();

			prevPoint.set(point);
			point.set(Math.cos(angle) * d, (i + 1F) * length / (options.segments() + 1F), Math.sin(angle) * d).mul(matrix);

			ms.pushPose();
			ms.translate(prevPoint.x, prevPoint.y, prevPoint.z);

			var rot = Rotation.compute(new Vec3(prevPoint.x, prevPoint.y, prevPoint.z), new Vec3(point.x, point.y, point.z));
			ms.mulPose(Axis.YP.rotationDegrees(-rot.yawDeg()));
			ms.mulPose(Axis.XP.rotationDegrees(90 + rot.pitchDeg()));

			quads(startRadius, endRadius, 0F, (float) prevPoint.distance(point), ms.last().transform(buffer));
			ms.popPose();
		}

		prevPoint.set(point);
		point.set(0D, length, 0D).mul(matrix);

		ms.pushPose();
		ms.translate(prevPoint.x, prevPoint.y, prevPoint.z);

		var rot = Rotation.compute(new Vec3(prevPoint.x, prevPoint.y, prevPoint.z), new Vec3(point.x, point.y, point.z));
		ms.mulPose(Axis.YP.rotationDegrees(-rot.yawDeg()));
		ms.mulPose(Axis.XP.rotationDegrees(90 + rot.pitchDeg()));

		quads(options.radius() * rmod, options.endingRadius() * rmod, 0F, (float) prevPoint.distance(point), ms.last().transform(buffer));
		ms.popPose();
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		if (length <= 0F) {
			return;
		}

		float time = KMath.lerp(delta, prevAge, age);

		if (time >= lifetime - 1F) {
			return;
		}

		var cameraPos = camera.getPosition();
		var rx = (float) (KMath.lerp(delta, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(delta, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(delta, zo, z) - cameraPos.z);

		ms.pushPose();
		ms.translate(rx, ry, rz);

		var prevPoint = new Vector3d(0D, 0D, 0D);
		var point = new Vector3d(0D, 0D, 0D);

		matrix.identity();
		matrix.rotateY(Math.toRadians(-direction.yawDeg()));
		matrix.rotateX(Math.toRadians(90 + direction.pitchDeg()));

		var brightBuffer = buffers.getBuffer(DebugRenderTypes.QUADS_NO_CULL).withColor(color.get(time / (float) lifetime));
		renderSegments(ms, prevPoint, point, delta, brightBuffer);

		point.set(0D, 0D, 0D);

		Bloom.markActive();
		var bloomBuffer = buffers.getBuffer(BloomRenderTypes.DEFAULT_POS_COL_NO_CULL).withColor(outlineColor.get(time / (float) lifetime));
		renderSegments(ms, prevPoint, point, delta, bloomBuffer);

		ms.popPose();
	}

	private static void quads(float sr, float er, float minY, float maxY, VertexCallback callback) {
		callback.acceptPos(-sr, minY, -sr).acceptTex(1F, 1F).acceptNormal(0F, 0F, -1F);
		callback.acceptPos(-er, maxY, -er).acceptTex(1F, 0F).acceptNormal(0F, 0F, -1F);
		callback.acceptPos(+er, maxY, -er).acceptTex(0F, 0F).acceptNormal(0F, 0F, -1F);
		callback.acceptPos(+sr, minY, -sr).acceptTex(0F, 1F).acceptNormal(0F, 0F, -1F);
		callback.acceptPos(+sr, minY, +sr).acceptTex(1F, 1F).acceptNormal(0F, 0F, 1F);
		callback.acceptPos(+er, maxY, +er).acceptTex(1F, 0F).acceptNormal(0F, 0F, 1F);
		callback.acceptPos(-er, maxY, +er).acceptTex(0F, 0F).acceptNormal(0F, 0F, 1F);
		callback.acceptPos(-sr, minY, +sr).acceptTex(0F, 1F).acceptNormal(0F, 0F, 1F);
		callback.acceptPos(-sr, minY, -sr).acceptTex(0F, 1F).acceptNormal(-1F, 0F, 0F);
		callback.acceptPos(-sr, minY, +sr).acceptTex(1F, 1F).acceptNormal(-1F, 0F, 0F);
		callback.acceptPos(-er, maxY, +er).acceptTex(1F, 0F).acceptNormal(-1F, 0F, 0F);
		callback.acceptPos(-er, maxY, -er).acceptTex(0F, 0F).acceptNormal(-1F, 0F, 0F);
		callback.acceptPos(+er, maxY, -er).acceptTex(1F, 0F).acceptNormal(1F, 0F, 0F);
		callback.acceptPos(+er, maxY, +er).acceptTex(0F, 0F).acceptNormal(1F, 0F, 0F);
		callback.acceptPos(+sr, minY, +sr).acceptTex(0F, 1F).acceptNormal(1F, 0F, 0F);
		callback.acceptPos(+sr, minY, -sr).acceptTex(1F, 1F).acceptNormal(1F, 0F, 0F);
	}

	@Override
	public AABB getRenderBoundingBox(float delta) {
		var rx = KMath.lerp(delta, xo, x);
		var ry = KMath.lerp(delta, yo, y);
		var rz = KMath.lerp(delta, zo, z);
		return new AABB(rx - length, ry - length, rz - length, rx + length, ry + length, rz + length);
	}
}
