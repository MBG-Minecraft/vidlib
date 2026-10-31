package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record NewCutsceneStep(
	Ref<NewCutsceneStepType> type,
	float start,
	Optional<Float> length,
	boolean snap
) {
	public static final Codec<NewCutsceneStep> CODEC = RecordCodecBuilder.create(i -> i.group(
		NewCutsceneStepType.CODEC.fieldOf("type").forGetter(NewCutsceneStep::type),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(NewCutsceneStep::start),
		Codec.FLOAT.optionalFieldOf("length").forGetter(NewCutsceneStep::length),
		Codec.BOOL.optionalFieldOf("snap", true).forGetter(NewCutsceneStep::snap)
	).apply(i, NewCutsceneStep::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, NewCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		NewCutsceneStepType.STREAM_CODEC, NewCutsceneStep::type,
		ByteBufCodecs.FLOAT, NewCutsceneStep::start,
		ByteBufCodecs.optional(ByteBufCodecs.FLOAT), NewCutsceneStep::length,
		ByteBufCodecs.BOOL, NewCutsceneStep::snap,
		NewCutsceneStep::new
	);
}
