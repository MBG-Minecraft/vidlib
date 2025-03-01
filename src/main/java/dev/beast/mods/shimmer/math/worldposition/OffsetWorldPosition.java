package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record OffsetWorldPosition(WorldPosition a, WorldPosition b) implements WorldPosition {
	public static final SimpleRegistryType<OffsetWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("offset"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldPosition.CODEC.fieldOf("a").forGetter(OffsetWorldPosition::a),
		WorldPosition.CODEC.fieldOf("b").forGetter(OffsetWorldPosition::b)
	).apply(instance, OffsetWorldPosition::new)), StreamCodec.composite(
		WorldPosition.STREAM_CODEC,
		OffsetWorldPosition::a,
		WorldPosition.STREAM_CODEC,
		OffsetWorldPosition::b,
		OffsetWorldPosition::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(Level level, float progress) {
		var a = this.a.get(level, progress);
		var b = this.b.get(level, progress);
		return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
}
