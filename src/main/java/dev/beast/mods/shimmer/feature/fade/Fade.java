package dev.beast.mods.shimmer.feature.fade;

import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.Easing;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Fade(Color color, int fadeInTicks, int pauseTicks, int fadeOutTicks, Easing fadeInEase, Easing fadeOutEase) {
	public static final StreamCodec<ByteBuf, Fade> STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, Fade::color,
		ByteBufCodecs.VAR_INT, Fade::fadeInTicks,
		ByteBufCodecs.VAR_INT, Fade::pauseTicks,
		ByteBufCodecs.VAR_INT, Fade::fadeOutTicks,
		Easing.STREAM_CODEC, Fade::fadeInEase,
		Easing.STREAM_CODEC, Fade::fadeOutEase,
		Fade::new
	);

	public Fade(Color color, int fadeInTicks, int pauseTicks, int fadeOutTicks) {
		this(color, fadeInTicks, pauseTicks, fadeOutTicks, Easing.LINEAR, Easing.LINEAR);
	}

	public Fade(Color color, int fadeInOutTicks, int pauseTicks) {
		this(color, fadeInOutTicks, pauseTicks, fadeInOutTicks);
	}
}
