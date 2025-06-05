package dev.latvian.mods.vidlib.feature.fade;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.function.Function;

public record Fade(Gradient color, int fadeInTicks, int pauseTicks, Optional<Integer> fadeOutTicks, Easing fadeInEase, Optional<Easing> fadeOutEase) {
	public static final Fade DEFAULT = new Fade(Color.BLACK, 20, 20, Optional.empty(), Easing.LINEAR, Optional.empty());
	public static final Fade SHORT = new Fade(Color.BLACK, 4, 2, Optional.empty(), Easing.LINEAR, Optional.empty());

	public static final Codec<Fade> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", Color.BLACK).forGetter(Fade::color),
		Codec.INT.optionalFieldOf("fade_in_ticks", 20).forGetter(Fade::fadeInTicks),
		Codec.INT.optionalFieldOf("pause_ticks", 20).forGetter(Fade::pauseTicks),
		Codec.INT.optionalFieldOf("fade_out_ticks").forGetter(Fade::fadeOutTicks),
		Easing.CODEC.optionalFieldOf("fade_in_ease", Easing.LINEAR).forGetter(Fade::fadeInEase),
		Easing.CODEC.optionalFieldOf("fade_out_ease").forGetter(Fade::fadeOutEase)
	).apply(instance, Fade::new));

	public static final Codec<Fade> CODEC = Codec.either(Codec.unit("short"), DIRECT_CODEC).xmap(e -> e.map(s -> switch (s) {
		case "short" -> SHORT;
		default -> DEFAULT;
	}, Function.identity()), v -> {
		if (v.equals(DEFAULT)) {
			return Either.left("default");
		} else if (v.equals(SHORT)) {
			return Either.left("short");
		} else {
			return Either.right(v);
		}
	});

	public static final StreamCodec<ByteBuf, Fade> STREAM_CODEC = CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, Fade::color,
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
