package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record FixedWorldPosition(Vec3 pos) implements WorldPosition {
	public static final SimpleRegistryType.Unit<FixedWorldPosition> ZERO = SimpleRegistryType.unit(Shimmer.id("zero"), new FixedWorldPosition(Vec3.ZERO));

	public static final SimpleRegistryType<FixedWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("fixed"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3.CODEC.fieldOf("pos").forGetter(FixedWorldPosition::pos)
	).apply(instance, FixedWorldPosition::new)), ShimmerStreamCodecs.VEC_3.map(FixedWorldPosition::new, FixedWorldPosition::pos));

	@Override
	public SimpleRegistryType<?> type() {
		return pos == Vec3.ZERO ? ZERO : TYPE;
	}

	@Override
	public Vec3 get(Level level, float progress) {
		return pos;
	}
}
