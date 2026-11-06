package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.shape.Shape;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShapeParticle extends CustomParticle {
	public static class State implements CustomParticleState {
		public double x;
		public double y;
		public double z;
		public float scale;
		public Shape shape;
		public Color color;
		public Color outlineColor;

		@Override
		public void submit(SubmitContext ctx) {
			ctx.push(x, y, z);
			ctx.poseStack().scale(scale, scale, scale);
			ctx.submitQuads(Identity.VEC_3, shape, color);
			ctx.submitLines(Identity.VEC_3, shape, outlineColor);
			ctx.pop();
		}
	}

	private final ShapeParticleOptions options;
	private final Ref<Shape> shape;
	private final Ref<Gradient> color;
	private final Ref<Gradient> outlineColor;
	public int prevAge;

	private State state;

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

		if (state == null) {
			state = new State();
		}

		float time = KMath.lerp(delta, prevAge, age);

		state.x = KMath.lerp(delta, xo, x);
		state.y = KMath.lerp(delta, yo, y);
		state.z = KMath.lerp(delta, zo, z);
		state.scale = options.lifespan() < 0 ? 0.5F : 1F;
		state.shape = options.shape().value();
		state.color = color.value().get(time / (float) lifetime).withAlpha(50).fadeOut(time, lifetime, 20F);
		state.outlineColor = outlineColor.value().get(time / (float) lifetime).fadeOut(time, lifetime, 20F);
		return state;
	}
}
