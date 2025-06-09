package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

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
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfWorldNumber::comparison),
		WorldNumber.CODEC.optionalFieldOf("value", FixedWorldNumber.ZERO.instance()).forGetter(IfWorldNumber::testValue),
		WorldNumber.CODEC.optionalFieldOf("then").forGetter(IfWorldNumber::thenValue),
		WorldNumber.CODEC.optionalFieldOf("else").forGetter(IfWorldNumber::elseValue)
	).apply(instance, IfWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, IfWorldNumber::ifValue,
		Comparison.DATA_TYPE.streamCodec().optional(Comparison.NOT_EQUALS), IfWorldNumber::comparison,
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
	@Nullable
	public Double get(WorldNumberContext ctx) {
		var i = ifValue.get(ctx);

		if (i == null) {
			return null;
		}

		var t = testValue.get(ctx);

		if (t == null) {
			return null;
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

		return null;
	}
}
