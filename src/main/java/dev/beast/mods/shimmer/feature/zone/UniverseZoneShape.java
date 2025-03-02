package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class UniverseZoneShape implements ZoneShape {
	public static final SimpleRegistryType<UniverseZoneShape> TYPE = SimpleRegistryType.unit(Shimmer.id("universe"), new UniverseZoneShape());

	private UniverseZoneShape() {
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneInstance instance, Vec3 start, Vec3 end) {
		return null;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return true;
	}

	@Override
	public boolean intersects(AABB box) {
		return true;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return Stream.empty();
	}
}
