package dev.latvian.mods.vidlib.feature.screeneffect.ca;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.JOMLCodecs;
import dev.latvian.mods.klib.codec.JOMLStreamCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.screeneffect.FocusPoint;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record FocusedChromaticAberrationEffect(FocusPoint focus, Ref<KNumber> strength, Vector3fc shift) implements ScreenEffect {
	public static final Vector3fc DEFAULT_SHIFT = new Vector3f(0F, 0.5F, 1F);

	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"focused_chromatic_aberration",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			FocusPoint.CODEC.optionalFieldOf("focus", FocusPoint.Screen.CENTER).forGetter(FocusedChromaticAberrationEffect::focus),
			KNumber.CODEC.optionalFieldOf("strength", KNumber.of(0.1D).ref()).forGetter(FocusedChromaticAberrationEffect::strength),
			JOMLCodecs.VEC3C.optionalFieldOf("shift", DEFAULT_SHIFT).forGetter(FocusedChromaticAberrationEffect::shift)
		).apply(instance, FocusedChromaticAberrationEffect::new)),
		CompositeStreamCodec.of(
			KLibStreamCodecs.optional(FocusPoint.STREAM_CODEC, FocusPoint.Screen.CENTER), FocusedChromaticAberrationEffect::focus,
			KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.of(0.1D).ref()), FocusedChromaticAberrationEffect::strength,
			KLibStreamCodecs.optional(JOMLStreamCodecs.VEC3C, DEFAULT_SHIFT), FocusedChromaticAberrationEffect::shift,
			FocusedChromaticAberrationEffect::new
		)
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
