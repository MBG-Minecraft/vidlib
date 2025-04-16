package dev.latvian.mods.vidlib.feature.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record Fade(Color color, int fadeInTicks, int pauseTicks, Optional<Integer> fadeOutTicks, Easing fadeInEase, Optional<Easing> fadeOutEase) {
	public static final Codec<Fade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Color.CODEC.optionalFieldOf("color", Color.BLACK).forGetter(Fade::color),
		Codec.INT.optionalFieldOf("fade_in_ticks", 20).forGetter(Fade::fadeInTicks),
		Codec.INT.optionalFieldOf("pause_ticks", 20).forGetter(Fade::pauseTicks),
		Codec.INT.optionalFieldOf("fade_out_ticks").forGetter(Fade::fadeOutTicks),
		Easing.CODEC.optionalFieldOf("fade_in_ease", Easing.LINEAR).forGetter(Fade::fadeInEase),
		Easing.CODEC.optionalFieldOf("fade_out_ease").forGetter(Fade::fadeOutEase)
	).apply(instance, Fade::new));

	public static final StreamCodec<ByteBuf, Fade> STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, Fade::color,
		ByteBufCodecs.VAR_INT, Fade::fadeInTicks,
		ByteBufCodecs.VAR_INT, Fade::pauseTicks,
		ByteBufCodecs.VAR_INT.optional(), Fade::fadeOutTicks,
		Easing.STREAM_CODEC.optional(Easing.LINEAR), Fade::fadeInEase,
		Easing.STREAM_CODEC.optional(), Fade::fadeOutEase,
		Fade::new
	);

	public Fade(Color color, int fadeInOutTicks, int pauseTicks) {
		this(color, fadeInOutTicks, pauseTicks, Optional.empty(), Easing.LINEAR, Optional.empty());
	}
}
