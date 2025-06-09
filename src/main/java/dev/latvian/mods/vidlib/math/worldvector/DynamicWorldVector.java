package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;

public record DynamicWorldVector(WorldNumber x, WorldNumber y, WorldNumber z) implements WorldVector {
	public static final SimpleRegistryType<DynamicWorldVector> TYPE = SimpleRegistryType.dynamic("dynamic", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("x").forGetter(DynamicWorldVector::x),
		WorldNumber.CODEC.fieldOf("y").forGetter(DynamicWorldVector::y),
		WorldNumber.CODEC.fieldOf("z").forGetter(DynamicWorldVector::z)
	).apply(instance, DynamicWorldVector::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, DynamicWorldVector::x,
		WorldNumber.STREAM_CODEC, DynamicWorldVector::y,
		WorldNumber.STREAM_CODEC, DynamicWorldVector::z,
		DynamicWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		var px = x.get(ctx);
		var py = y.get(ctx);
		var pz = z.get(ctx);

		if (px == null || py == null || pz == null) {
			return null;
		}

		return KMath.vec3(px, py, pz);
	}
}
