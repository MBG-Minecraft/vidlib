package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ScalarWorldVector(WorldNumber number) implements WorldVector {
	public static final SimpleRegistryType<ScalarWorldVector> TYPE = SimpleRegistryType.dynamic("scalar", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("number").forGetter(ScalarWorldVector::number)
	).apply(instance, ScalarWorldVector::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, ScalarWorldVector::number,
		ScalarWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var n = number.get(ctx);

		if (Double.isNaN(n)) {
			return null;
		}

		return n == 0D ? Vec3.ZERO : new Vec3(n, n, n);
	}
}
