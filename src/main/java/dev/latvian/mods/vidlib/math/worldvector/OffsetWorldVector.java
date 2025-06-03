package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record OffsetWorldVector(WorldVector a, WorldVector b) implements WorldVector {
	public static final SimpleRegistryType<OffsetWorldVector> TYPE = SimpleRegistryType.dynamic("offset", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldVector.CODEC.fieldOf("a").forGetter(OffsetWorldVector::a),
		WorldVector.CODEC.fieldOf("b").forGetter(OffsetWorldVector::b)
	).apply(instance, OffsetWorldVector::new)), CompositeStreamCodec.of(
		WorldVector.STREAM_CODEC, OffsetWorldVector::a,
		WorldVector.STREAM_CODEC, OffsetWorldVector::b,
		OffsetWorldVector::new
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
		return a == null || b == null ? null : new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
}
