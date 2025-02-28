package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EmptyZoneShape implements ZoneShape {
	private static final AABB BOX = new AABB(0D, 0D, 0D, 0D, 0D, 0D);
	public static final SimpleRegistryType.Unit<EmptyZoneShape> TYPE = SimpleRegistryType.unit(Shimmer.id("empty"), new EmptyZoneShape());

	private EmptyZoneShape() {
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return BOX;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(Vec3 start, Vec3 end) {
		return null;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return false;
	}

	@Override
	public boolean intersects(AABB box) {
		return false;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return Stream.empty();
	}

	@Override
	public List<Entity> collectEntities(Level level, Predicate<? super Entity> predicate) {
		return List.of();
	}
}
