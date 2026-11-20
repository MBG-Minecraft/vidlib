package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CutsceneStep(
	Ref<CutsceneStepType> type,
	float start,
	float length,
	boolean snap
) {
	public static final Codec<CutsceneStep> CODEC = RecordCodecBuilder.create(i -> i.group(
		CutsceneStepType.CODEC.fieldOf("type").forGetter(CutsceneStep::type),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(CutsceneStep::start),
		Codec.FLOAT.optionalFieldOf("length", 0F).forGetter(CutsceneStep::length),
		Codec.BOOL.optionalFieldOf("snap", true).forGetter(CutsceneStep::snap)
	).apply(i, CutsceneStep::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		CutsceneStepType.STREAM_CODEC, CutsceneStep::type,
		KLibStreamCodecs.FLOAT_OR_ZERO, CutsceneStep::start,
		KLibStreamCodecs.FLOAT_OR_ZERO, CutsceneStep::length,
		ByteBufCodecs.BOOL, CutsceneStep::snap,
		CutsceneStep::new
	);
}
