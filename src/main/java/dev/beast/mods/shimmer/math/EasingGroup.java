package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public class EasingGroup {
	public static final Map<String, EasingGroup> GROUPS = new LinkedHashMap<>();
	public static final Codec<EasingGroup> CODEC = Codec.STRING.xmap(GROUPS::get, EasingGroup::toString);
	public static final StreamCodec<ByteBuf, EasingGroup> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(GROUPS::get, EasingGroup::toString);

	public static EasingGroup add(String id, Easing in, Easing out, Easing inOut) {
		var group = new EasingGroup(id, in, out, inOut);
		GROUPS.put(id, group);
		return group;
	}

	public static EasingGroup add(String id, Easing easing) {
		return add(id, easing, easing, easing);
	}

	public static final EasingGroup LINEAR = add("linear", Easing.LINEAR);
	public static final EasingGroup SMOOTHSTEP = add("smoothstep", Easing.SMOOTHSTEP);
	public static final EasingGroup ISMOOTHSTEP = add("ismoothstep", Easing.ISMOOTHSTEP);
	public static final EasingGroup SMOOTHERSTEP = add("smootherstep", Easing.SMOOTHERSTEP);
	public static final EasingGroup SINE = add("sine", Easing.SINE_IN, Easing.SINE_OUT, Easing.SINE_IN_OUT);
	public static final EasingGroup QUAD = add("quad", Easing.QUAD_IN, Easing.QUAD_OUT, Easing.QUAD_IN_OUT);
	public static final EasingGroup CUBIC = add("cubic", Easing.CUBIC_IN, Easing.CUBIC_OUT, Easing.CUBIC_IN_OUT);
	public static final EasingGroup QUART = add("quart", Easing.QUART_IN, Easing.QUART_OUT, Easing.QUART_IN_OUT);
	public static final EasingGroup QUINT = add("quint", Easing.QUINT_IN, Easing.QUINT_OUT, Easing.QUINT_IN_OUT);
	public static final EasingGroup EXPO = add("expo", Easing.EXPO_IN, Easing.EXPO_OUT, Easing.EXPO_IN_OUT);
	public static final EasingGroup CIRC = add("circ", Easing.CIRC_IN, Easing.CIRC_OUT, Easing.CIRC_IN_OUT);
	public static final EasingGroup BACK = add("back", Easing.BACK_IN, Easing.BACK_OUT, Easing.BACK_IN_OUT);
	public static final EasingGroup ELASTIC = add("elastic", Easing.ELASTIC_IN, Easing.ELASTIC_OUT, Easing.ELASTIC_IN_OUT);
	public static final EasingGroup BOUNCE = add("bounce", Easing.BOUNCE_IN, Easing.BOUNCE_OUT, Easing.BOUNCE_IN_OUT);

	public final String id;
	public final Easing in;
	public final Easing out;
	public final Easing inOut;

	private EasingGroup(String id, Easing in, Easing out, Easing inOut) {
		this.id = id;
		this.in = in;
		this.out = out;
		this.inOut = inOut;
	}

	@Override
	public String toString() {
		return id;
	}

	public double easeMirrored(double x, EasingGroup end) {
		return x < 0.5D ? out.ease(x * 2D) : 1D - end.out.ease((x - 0.5D) * 2D);
	}

	public double easeMirrored(double x) {
		return easeMirrored(x, this);
	}
}