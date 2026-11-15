package dev.latvian.mods.vidlib.feature.screeneffect.ca;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.JOMLCodecs;
import dev.latvian.mods.klib.codec.JOMLStreamCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record AngledChromaticAberrationEffect(Ref<KNumber> strength, Ref<KNumber> angle, Vector3fc shift) implements ScreenEffect {
	public static final Vector3fc DEFAULT_SHIFT = new Vector3f(-1F, 0F, 1F);

	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"angled_chromatic_aberration",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			KNumber.CODEC.optionalFieldOf("strength", KNumber.of(0.1D).ref()).forGetter(AngledChromaticAberrationEffect::strength),
			KNumber.CODEC.optionalFieldOf("angle", KNumber.ZERO).forGetter(AngledChromaticAberrationEffect::angle),
			JOMLCodecs.VEC3C.optionalFieldOf("shift", DEFAULT_SHIFT).forGetter(AngledChromaticAberrationEffect::shift)
		).apply(instance, AngledChromaticAberrationEffect::new)),
		CompositeStreamCodec.of(
			KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.of(0.1D).ref()), AngledChromaticAberrationEffect::strength,
			KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), AngledChromaticAberrationEffect::angle,
			KLibStreamCodecs.optional(JOMLStreamCodecs.VEC3C, DEFAULT_SHIFT), AngledChromaticAberrationEffect::shift,
			AngledChromaticAberrationEffect::new
		)
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
