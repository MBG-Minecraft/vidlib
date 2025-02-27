package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class UniverseZoneShape implements ZoneShape {
	public static final UniverseZoneShape INSTANCE = new UniverseZoneShape();
	public static final ZoneShapeType<UniverseZoneShape> TYPE = new ZoneShapeType<>("universe", MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	private UniverseZoneShape() {
	}

	@Override
	public ZoneShapeType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(Vec3 start, Vec3 end) {
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
