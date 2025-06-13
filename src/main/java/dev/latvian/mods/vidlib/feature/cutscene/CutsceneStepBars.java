package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.Empty;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record CutsceneStepBars(Optional<Component> top, Optional<Component> bottom) {
	public static final CutsceneStepBars NONE = new CutsceneStepBars(Optional.empty(), Optional.empty());
	public static final CutsceneStepBars DEFAULT = new CutsceneStepBars(Optional.of(Empty.COMPONENT), Optional.of(Empty.COMPONENT));

	public static final Codec<CutsceneStepBars> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ComponentSerialization.CODEC.optionalFieldOf("top").forGetter(CutsceneStepBars::top),
		ComponentSerialization.CODEC.optionalFieldOf("bottom").forGetter(CutsceneStepBars::bottom)
	).apply(instance, CutsceneStepBars::new));

	public static final Codec<CutsceneStepBars> CODEC = Codec.either(DIRECT_CODEC, Codec.BOOL).xmap(e -> e.map(b -> b.equals(DEFAULT) ? DEFAULT : b.equals(NONE) ? NONE : b, b -> b ? DEFAULT : NONE), b -> b.equals(DEFAULT) ? Either.right(true) : b.equals(NONE) ? Either.right(false) : Either.left(b));

	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStepBars> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.STREAM_CODEC.optional(), CutsceneStepBars::top,
		ComponentSerialization.STREAM_CODEC.optional(), CutsceneStepBars::bottom,
		CutsceneStepBars::new
	);

	public static final DataType<CutsceneStepBars> DIRECT_DATA_TYPE = DataType.build(CutsceneStepBars.class,
		DataTypes.TEXT_COMPONENT.optionalField("top", CutsceneStepBars::top),
		DataTypes.TEXT_COMPONENT.optionalField("bottom", CutsceneStepBars::bottom),
		CutsceneStepBars::new
	);

	public static final DataType<CutsceneStepBars> DATA_TYPE = DataType.either(
		DIRECT_DATA_TYPE,
		DataTypes.BOOL,
		b -> b.equals(DEFAULT) ? DEFAULT : b.equals(NONE) ? NONE : b,
		b -> b ? DEFAULT : NONE,
		b -> b.equals(DEFAULT) ? Either.right(true) : b.equals(NONE) ? Either.right(false) : Either.left(b),
		CutsceneStepBars.class
	);
}
