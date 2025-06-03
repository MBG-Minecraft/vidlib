package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ScaledWorldVector(WorldVector a, WorldVector b) implements WorldVector {
	public static final SimpleRegistryType<ScaledWorldVector> TYPE = SimpleRegistryType.dynamic("scaled", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldVector.CODEC.fieldOf("a").forGetter(ScaledWorldVector::a),
		WorldVector.CODEC.fieldOf("b").forGetter(ScaledWorldVector::b)
	).apply(instance, ScaledWorldVector::new)), CompositeStreamCodec.of(
		WorldVector.STREAM_CODEC, ScaledWorldVector::a,
		WorldVector.STREAM_CODEC, ScaledWorldVector::b,
		ScaledWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);
		return a == null || b == null ? null : new Vec3(a.x * b.x, a.y * b.y, a.z * b.z);
	}
}
