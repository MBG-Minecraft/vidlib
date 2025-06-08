package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

import java.util.Optional;

public record IfWorldNumber(
	WorldNumber ifValue,
	Comparison comparison,
	WorldNumber testValue,
	Optional<WorldNumber> thenValue,
	Optional<WorldNumber> elseValue
) implements WorldNumber {
	public static final SimpleRegistryType<IfWorldNumber> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("if").forGetter(IfWorldNumber::ifValue),
		Comparison.CODEC.optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfWorldNumber::comparison),
		WorldNumber.CODEC.optionalFieldOf("value", FixedWorldNumber.ZERO.instance()).forGetter(IfWorldNumber::testValue),
		WorldNumber.CODEC.optionalFieldOf("then").forGetter(IfWorldNumber::thenValue),
		WorldNumber.CODEC.optionalFieldOf("else").forGetter(IfWorldNumber::elseValue)
	).apply(instance, IfWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, IfWorldNumber::ifValue,
		Comparison.STREAM_CODEC.optional(Comparison.NOT_EQUALS), IfWorldNumber::comparison,
		WorldNumber.STREAM_CODEC.optional(FixedWorldNumber.ZERO.instance()), IfWorldNumber::testValue,
		WorldNumber.STREAM_CODEC.optional(), IfWorldNumber::thenValue,
		WorldNumber.STREAM_CODEC.optional(), IfWorldNumber::elseValue,
		IfWorldNumber::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double get(WorldNumberContext ctx) {
		var i = ifValue.get(ctx);

		if (Double.isNaN(i)) {
			return Double.NaN;
		}

		var t = testValue.get(ctx);

		if (Double.isNaN(t)) {
			return Double.NaN;
		}

		if (comparison.test(i, t)) {
			if (thenValue.isPresent()) {
				return thenValue.get().get(ctx);
			}
		} else {
			if (elseValue.isPresent()) {
				return elseValue.get().get(ctx);
			}
		}

		return Double.NaN;
	}
}
