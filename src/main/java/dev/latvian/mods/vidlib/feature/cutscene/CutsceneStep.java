package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.cutscene.event.CutsceneEvent;
import dev.latvian.mods.vidlib.feature.fade.Fade;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record CutsceneStep(
	int start,
	KNumber length,
	Optional<KVector> origin,
	Optional<KVector> target,
	Optional<KNumber> fovModifier,
	Optional<Component> status,
	Optional<CutsceneStepBars> bars,
	Optional<ResourceLocation> shader,
	Optional<Fade> fade,
	List<PositionedSoundData> sounds,
	CutsceneStepSnap snap,
	List<CutsceneEvent> events
) {
	public static final Codec<CutsceneStep> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("start", 0).forGetter(CutsceneStep::start),
		KNumber.CODEC.optionalFieldOf("length", KNumber.ZERO).forGetter(CutsceneStep::length),
		KVector.CODEC.optionalFieldOf("origin").forGetter(CutsceneStep::origin),
		KVector.CODEC.optionalFieldOf("target").forGetter(CutsceneStep::target),
		KNumber.CODEC.optionalFieldOf("fov_modifier").forGetter(CutsceneStep::fovModifier),
		ComponentSerialization.CODEC.optionalFieldOf("status").forGetter(CutsceneStep::status),
		CutsceneStepBars.CODEC.optionalFieldOf("bars").forGetter(CutsceneStep::bars),
		ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(CutsceneStep::shader),
		Fade.CODEC.optionalFieldOf("fade").forGetter(CutsceneStep::fade),
		PositionedSoundData.CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(CutsceneStep::sounds),
		CutsceneStepSnap.CODEC.optionalFieldOf("snap", CutsceneStepSnap.NONE).forGetter(CutsceneStep::snap),
		CutsceneEvent.CODEC.listOf().optionalFieldOf("events", List.of()).forGetter(CutsceneStep::events)
	).apply(instance, CutsceneStep::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, CutsceneStep::start,
		KNumber.STREAM_CODEC.optional(KNumber.ZERO), CutsceneStep::length,
		KVector.STREAM_CODEC.optional(), CutsceneStep::origin,
		KVector.STREAM_CODEC.optional(), CutsceneStep::target,
		KNumber.STREAM_CODEC.optional(), CutsceneStep::fovModifier,
		ComponentSerialization.STREAM_CODEC.optional(), CutsceneStep::status,
		CutsceneStepBars.STREAM_CODEC.optional(), CutsceneStep::bars,
		ResourceLocation.STREAM_CODEC.optional(), CutsceneStep::shader,
		Fade.STREAM_CODEC.optional(), CutsceneStep::fade,
		PositionedSoundData.STREAM_CODEC.listOf(), CutsceneStep::sounds,
		CutsceneStepSnap.STREAM_CODEC, CutsceneStep::snap,
		CutsceneEvent.REGISTRY.valueStreamCodec().listOf(), CutsceneStep::events,
		CutsceneStep::new
	);
}
