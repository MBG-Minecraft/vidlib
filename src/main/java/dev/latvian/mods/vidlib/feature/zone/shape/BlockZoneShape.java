package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public record BlockZoneShape(BlockPos start, BlockPos end, AABB box) implements ZoneShape {
	public static final SimpleRegistryType<BlockZoneShape> TYPE = SimpleRegistryType.dynamic("block", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(BlockZoneShape::start),
		BlockPos.CODEC.fieldOf("end").forGetter(BlockZoneShape::end)
	).apply(instance, BlockZoneShape::new)), CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, BlockZoneShape::start,
		BlockPos.STREAM_CODEC, BlockZoneShape::end,
		BlockZoneShape::new
	));

	public BlockZoneShape(BlockPos start, BlockPos end) {
		this(start, end, AABB.encapsulatingFullBlocks(start, end));
	}

	public BlockZoneShape(BlockPos pos) {
		this(pos, pos);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}

	@Override
	public boolean contains(Vec3i pos) {
		return pos.getX() >= Math.min(start.getX(), end.getX())
			&& pos.getX() <= Math.max(start.getX(), end.getX())
			&& pos.getY() >= Math.min(start.getY(), end.getY())
			&& pos.getY() <= Math.max(start.getY(), end.getY())
			&& pos.getZ() >= Math.min(start.getZ(), end.getZ())
			&& pos.getZ() <= Math.max(start.getZ(), end.getZ());
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(start, end);
	}

	public AAIBB toAAIBB() {
		return new AAIBB(start, end);
	}

	@Override
	public ZoneShape move(double x, double y, double z) {
		int ix = Mth.floor(x);
		int iy = Mth.floor(y);
		int iz = Mth.floor(z);

		if (ix == x && iy == y && iz == z) {
			return new BlockZoneShape(start.offset(ix, iy, iz), end.offset(ix, iy, iz), box.move(x, y, z));
		}

		return ZoneShape.super.move(x, y, z);
	}
}
