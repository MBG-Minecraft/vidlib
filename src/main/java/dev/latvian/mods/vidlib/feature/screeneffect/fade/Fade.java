package dev.latvian.mods.vidlib.feature.screeneffect.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.klib.interpolation.LinearInterpolation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Fade(Gradient color, int fadeInTicks, int pauseTicks, Optional<Integer> fadeOutTicks, Interpolation fadeInInterpolation, Optional<Interpolation> fadeOutInterpolation) {
	public static final Fade DEFAULT = new Fade(Color.BLACK, 20, 20, Optional.empty(), LinearInterpolation.INSTANCE, Optional.empty());
	public static final Fade SHORT = new Fade(Color.BLACK, 4, 2, Optional.empty(), LinearInterpolation.INSTANCE, Optional.empty());

	public static final Codec<Fade> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", Color.BLACK).forGetter(Fade::color),
		KLibCodecs.TICKS.optionalFieldOf("fade_in_ticks", 20).forGetter(Fade::fadeInTicks),
		KLibCodecs.TICKS.optionalFieldOf("pause_ticks", 20).forGetter(Fade::pauseTicks),
		KLibCodecs.TICKS.optionalFieldOf("fade_out_ticks").forGetter(Fade::fadeOutTicks),
		Interpolation.CODEC.optionalFieldOf("fade_in_interpolation", LinearInterpolation.INSTANCE).forGetter(Fade::fadeInInterpolation),
		Interpolation.CODEC.optionalFieldOf("fade_out_interpolation").forGetter(Fade::fadeOutInterpolation)
	).apply(instance, Fade::new));

	public static final Codec<Fade> LITERAL_CODEC = KLibCodecs.partialMap(Map.of(
		"default", DEFAULT,
		"short", SHORT
	), Codec.STRING, false);

	public static final Codec<Fade> CODEC = KLibCodecs.or(List.of(LITERAL_CODEC, DIRECT_CODEC));

	public static final StreamCodec<ByteBuf, Fade> STREAM_CODEC = CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, Fade::color,
		ByteBufCodecs.VAR_INT, Fade::fadeInTicks,
		ByteBufCodecs.VAR_INT, Fade::pauseTicks,
		ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), Fade::fadeOutTicks,
		Interpolation.STREAM_CODEC, Fade::fadeInInterpolation,
		ByteBufCodecs.optional(Interpolation.STREAM_CODEC), Fade::fadeOutInterpolation,
		Fade::new
	);

	public Fade(Gradient color, int fadeInOutTicks, int pauseTicks) {
		this(color, fadeInOutTicks, pauseTicks, Optional.empty(), LinearInterpolation.INSTANCE, Optional.empty());
	}
}
