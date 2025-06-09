package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IfWorldVector(
	WorldNumber ifValue,
	Comparison comparison,
	WorldNumber testValue,
	Optional<WorldVector> thenValue,
	Optional<WorldVector> elseValue
) implements WorldVector {
	public static final SimpleRegistryType<IfWorldVector> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("if").forGetter(IfWorldVector::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfWorldVector::comparison),
		WorldNumber.CODEC.optionalFieldOf("value", FixedWorldNumber.ZERO.instance()).forGetter(IfWorldVector::testValue),
		WorldVector.CODEC.optionalFieldOf("then").forGetter(IfWorldVector::thenValue),
		WorldVector.CODEC.optionalFieldOf("else").forGetter(IfWorldVector::elseValue)
	).apply(instance, IfWorldVector::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, IfWorldVector::ifValue,
		Comparison.DATA_TYPE.streamCodec().optional(Comparison.NOT_EQUALS), IfWorldVector::comparison,
		WorldNumber.STREAM_CODEC.optional(FixedWorldNumber.ZERO.instance()), IfWorldVector::testValue,
		WorldVector.STREAM_CODEC.optional(), IfWorldVector::thenValue,
		WorldVector.STREAM_CODEC.optional(), IfWorldVector::elseValue,
		IfWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
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
