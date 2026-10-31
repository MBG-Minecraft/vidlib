package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.shape.Shape;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShapeParticle extends CustomParticle {
	public record State(
		float x,
		float y,
		float z,
		float scale,
		Shape shape,
		Color color,
		Color outlineColor
	) implements CustomParticleState {
		@Override
		public void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
			var ms = new PoseStack();
			ms.translate(x, y, z);
			ms.scale(scale, scale, scale);
			collector.submitCustomGeometry(ms, RenderTypes.debugFilledBox(), (pose, buffer) -> shape.buildQuads(pose.transform(buffer).withColor(color)));
			collector.submitCustomGeometry(ms, RenderTypes.lines(), (pose, buffer) -> shape.buildLines(pose.transform(buffer).withColor(outlineColor)));
		}
	}

	private final ShapeParticleOptions options;
	private final Ref<Shape> shape;
	private final Ref<Gradient> color;
	private final Ref<Gradient> outlineColor;
	public int prevAge;

	protected ShapeParticle(ShapeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
		super(level, x, y, z);
		this.options = options;
		this.shape = options.shape();
		this.color = options.color();
		this.outlineColor = options.outlineColor();
		setLifetime(Math.max(1, Math.abs(options.lifespan())));
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();
	}

	@Override
	@Nullable
	public State extract(Frustum frustum, Camera camera, Vec3 cameraPos, float delta) {
		if (!shape.value().isVisible(x, y, z, frustum)) {
			return null;
		}

		float time = KMath.lerp(delta, prevAge, age);

		return new State(
			(float) (KMath.lerp(delta, xo, x) - cameraPos.x),
			(float) (KMath.lerp(delta, yo, y) - cameraPos.y),
			(float) (KMath.lerp(delta, zo, z) - cameraPos.z),
			options.lifespan() < 0 ? 0.5F : 1F,
			options.shape().value(),
			color.value().get(time / (float) lifetime).withAlpha(50).fadeOut(time, lifetime, 20F),
			outlineColor.value().get(time / (float) lifetime).fadeOut(time, lifetime, 20F)
		);
	}
}
