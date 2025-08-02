package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Empty;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CutsceneStepBars(Optional<Component> top, Optional<Component> bottom) {
	public static final CutsceneStepBars DEFAULT = new CutsceneStepBars(Optional.empty(), Optional.empty());
	public static final CutsceneStepBars CLEAR = new CutsceneStepBars(Optional.of(Empty.COMPONENT), Optional.of(Empty.COMPONENT));

	public static CutsceneStepBars of(Optional<Component> top, Optional<Component> bottom) {
		if (top.isEmpty() && bottom.isEmpty()) {
			return DEFAULT;
		} else if (top.isPresent() && bottom.isPresent() && Empty.isEmpty(top.get()) && Empty.isEmpty(bottom.get())) {
			return CLEAR;
		} else {
			return new CutsceneStepBars(top, bottom);
		}
	}

	public static final Codec<CutsceneStepBars> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ComponentSerialization.CODEC.optionalFieldOf("top").forGetter(CutsceneStepBars::top),
		ComponentSerialization.CODEC.optionalFieldOf("bottom").forGetter(CutsceneStepBars::bottom)
	).apply(instance, CutsceneStepBars::of));

	public static final Codec<CutsceneStepBars> LITERAL_CODEC = KLibCodecs.partialMap(Map.of(
		"default", DEFAULT,
		"clear", CLEAR
	), Codec.STRING, false);

	public static final Codec<CutsceneStepBars> CODEC = KLibCodecs.or(List.of(LITERAL_CODEC, DIRECT_CODEC));

	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStepBars> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC), CutsceneStepBars::top,
		ByteBufCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC), CutsceneStepBars::bottom,
		CutsceneStepBars::of
	);
}
