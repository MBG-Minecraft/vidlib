package dev.latvian.mods.vidlib.feature.screeneffect.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.EasingType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Fade(Ref<Gradient> color, int fadeInTicks, int pauseTicks, Optional<Integer> fadeOutTicks, EasingType fadeInEase, Optional<EasingType> fadeOutEase) {
	public static final Fade DEFAULT = new Fade(Color.BLACK.toGradient().ref(), 20, 20, Optional.empty(), EasingType.LINEAR, Optional.empty());
	public static final Fade SHORT = new Fade(Color.BLACK.toGradient().ref(), 4, 2, Optional.empty(), EasingType.LINEAR, Optional.empty());

	public static final Codec<Fade> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", DEFAULT.color).forGetter(Fade::color),
		KLibCodecs.TICKS.optionalFieldOf("fade_in_ticks", 20).forGetter(Fade::fadeInTicks),
		KLibCodecs.TICKS.optionalFieldOf("pause_ticks", 20).forGetter(Fade::pauseTicks),
		KLibCodecs.TICKS.optionalFieldOf("fade_out_ticks").forGetter(Fade::fadeOutTicks),
		EasingType.CODEC.optionalFieldOf("fade_in_ease", EasingType.LINEAR).forGetter(Fade::fadeInEase),
		EasingType.CODEC.optionalFieldOf("fade_out_ease").forGetter(Fade::fadeOutEase)
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
		MCStreamCodecs.EASING_TYPE, Fade::fadeInEase,
		ByteBufCodecs.optional(MCStreamCodecs.EASING_TYPE), Fade::fadeOutEase,
		Fade::new
	);

	public Fade(Ref<Gradient> color, int fadeInOutTicks, int pauseTicks) {
		this(color, fadeInOutTicks, pauseTicks, Optional.empty(), EasingType.LINEAR, Optional.empty());
	}
}
