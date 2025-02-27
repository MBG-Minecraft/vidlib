package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <a href="https://easings.net/">Source</a>
 */
public final class Easing {
	public static final Map<String, Easing> FUNCTIONS = new LinkedHashMap<>();
	public static final Codec<Easing> CODEC = Codec.STRING.xmap(FUNCTIONS::get, Easing::toString);
	public static final StreamCodec<ByteBuf, Easing> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(FUNCTIONS::get, Easing::toString);

	public static Easing add(String id, Double2DoubleFunction function) {
		var easing = new Easing(id, function);
		FUNCTIONS.put(id, easing);
		return easing;
	}

	public static final Easing LINEAR = add("linear", x -> x);
	public static final Easing SMOOTHSTEP = add("smoothstep", KMath::smoothstep);
	public static final Easing ISMOOTHSTEP = add("ismoothstep", KMath::ismoothstep);
	public static final Easing SMOOTHERSTEP = add("smootherstep", KMath::smootherstep);

	public static final Easing SINE_IN = add("sine_in", x -> 1 - Math.cos((x * Math.PI) / 2));
	public static final Easing SINE_OUT = add("sine_out", x -> Math.sin((x * Math.PI) / 2));
	public static final Easing SINE_IN_OUT = add("sine_in_out", x -> -(Math.cos(Math.PI * x) - 1) / 2);

	public static final Easing QUAD_IN = add("quad_in", x -> x * x);
	public static final Easing QUAD_OUT = add("quad_out", x -> 1 - (1 - x) * (1 - x));
	public static final Easing QUAD_IN_OUT = add("quad_in_out", x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);

	public static final Easing CUBIC_IN = add("cubic_in", x -> x * x * x);
	public static final Easing CUBIC_OUT = add("cubic_out", x -> 1 - Math.pow(1 - x, 3));
	public static final Easing CUBIC_IN_OUT = add("cubic_in_out", x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2D);

	public static final Easing QUART_IN = add("quart_in", x -> x * x * x * x);
	public static final Easing QUART_OUT = add("quart_out", x -> 1 - Math.pow(1 - x, 4));
	public static final Easing QUART_IN_OUT = add("quart_in_out", x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2D);

	public static final Easing QUINT_IN = add("quint_in", x -> x * x * x * x * x);
	public static final Easing QUINT_OUT = add("quint_out", x -> 1 - Math.pow(1 - x, 5));
	public static final Easing QUINT_IN_OUT = add("quint_in_out", x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2D);

	public static final Easing EXPO_IN = add("expo_in", x -> x == 0 ? 0 : Math.pow(2, 10 * x - 10));
	public static final Easing EXPO_OUT = add("expo_out", x -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x));
	public static final Easing EXPO_IN_OUT = add("expo_in_out", x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2);

	public static final Easing CIRC_IN = add("circ_in", x -> 1 - Math.sqrt(1 - x * x));
	public static final Easing CIRC_OUT = add("circ_out", x -> Math.sqrt(1 - (x - 1) * (x - 1)));
	public static final Easing CIRC_IN_OUT = add("circ_in_out", x -> x < 0.5 ? (1 - Math.sqrt(1 - 4 * x * x)) / 2 : (Math.sqrt(1 - (-2 * x + 2) * (-2 * x + 2)) + 1) / 2);

	public static final Easing BACK_IN = add("back_in", x -> x * x * (2.70158 * x - 1.70158));
	public static final Easing BACK_OUT = add("back_out", x -> 1 - (1 - x) * (1 - x) * (2.70158 * (1 - x) - 1.70158));
	public static final Easing BACK_IN_OUT = add("back_in_out", x -> x < 0.5 ? Math.pow(2 * x, 2) * ((2.5949095 + 1) * 2 * x - 2.5949095) / 2 : (Math.pow(2 * x - 2, 2) * ((2.5949095 + 1) * (x * 2 - 2) + 2.5949095) + 2) / 2);

	public static final Easing ELASTIC_IN = add("elastic_in", x -> Math.sin(13 * Math.PI / 2 * x) * Math.pow(2, 10 * x - 10));
	public static final Easing ELASTIC_OUT = add("elastic_out", x -> Math.sin(-13 * Math.PI / 2 * (x + 1)) * Math.pow(2, -10 * x) + 1);
	public static final Easing ELASTIC_IN_OUT = add("elastic_in_out", x -> x < 0.5 ? Math.sin(13 * Math.PI / 2 * (2 * x)) * Math.pow(2, 10 * (2 * x) - 10) / 2 : Math.sin(-13 * Math.PI / 2 * (2 * x - 1)) * Math.pow(2, -10 * (2 * x - 1)) / 2 + 1);

	public static final Easing BOUNCE_OUT = add("bounce_out", x -> {
		if (x < 1 / 2.75) {
			return 7.5625 * x * x;
		} else if (x < 2 / 2.75) {
			return 7.5625 * (x -= 1.5 / 2.75) * x + 0.75;
		} else if (x < 2.5 / 2.75) {
			return 7.5625 * (x -= 2.25 / 2.75) * x + 0.9375;
		} else {
			return 7.5625 * (x -= 2.625 / 2.75) * x + 0.984375;
		}
	});

	public static final Easing BOUNCE_IN = add("bounce_in", x -> 1 - BOUNCE_OUT.ease(1 - x));
	public static final Easing BOUNCE_IN_OUT = add("bounce_in_out", x -> x < 0.5 ? BOUNCE_IN.ease(x * 2) / 2 : BOUNCE_OUT.ease(x * 2 - 1) / 2 + 0.5);

	public final String id;
	public final Double2DoubleFunction function;

	private Easing(String id, Double2DoubleFunction function) {
		this.id = id;
		this.function = function;
	}

	public double ease(double x) {
		return function.get(x);
	}

	public double easeClamped(double x) {
		return function.get(KMath.clamp(x, 0D, 1D));
	}

	public double lerp(double t, double a, double b) {
		return KMath.lerp(function.get(t), a, b);
	}

	public Vec3 lerp(double t, Vec3 a, Vec3 b) {
		var e = function.get(t);
		return new Vec3(KMath.lerp(e, a.x, b.x), KMath.lerp(e, a.y, b.y), KMath.lerp(e, a.z, b.z));
	}

	@Override
	public String toString() {
		return id;
	}
}
