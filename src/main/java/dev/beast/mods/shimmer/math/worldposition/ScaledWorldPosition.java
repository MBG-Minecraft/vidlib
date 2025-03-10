package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ScaledWorldPosition(WorldPosition a, WorldPosition b) implements WorldPosition {
	public static final SimpleRegistryType<ScaledWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("scaled"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldPosition.CODEC.fieldOf("a").forGetter(ScaledWorldPosition::a),
		WorldPosition.CODEC.fieldOf("b").forGetter(ScaledWorldPosition::b)
	).apply(instance, ScaledWorldPosition::new)), CompositeStreamCodec.of(
		WorldPosition.STREAM_CODEC, ScaledWorldPosition::a,
		WorldPosition.STREAM_CODEC, ScaledWorldPosition::b,
		ScaledWorldPosition::new
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
