package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public record BlockZoneShape(BlockPos start, BlockPos end, AABB box, AAIBB intBox) implements ZoneShape {
	public static BlockZoneShape of(BlockPos start, BlockPos end) {
		var start0 = BlockPos.min(start, end);
		var end0 = BlockPos.max(start, end);
		return new BlockZoneShape(start0, end0, AABB.encapsulatingFullBlocks(start0, end0), new AAIBB(start0, end0));
	}

	public static BlockZoneShape of(BlockPos pos) {
		return of(pos, pos);
	}

	public static final SimpleRegistryType<BlockZoneShape> TYPE = SimpleRegistryType.dynamic("block", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(BlockZoneShape::start),
		BlockPos.CODEC.fieldOf("end").forGetter(BlockZoneShape::end)
	).apply(instance, BlockZoneShape::of)), CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, BlockZoneShape::start,
		BlockPos.STREAM_CODEC, BlockZoneShape::end,
		BlockZoneShape::of
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB toAABB() {
		return box;
	}

	@Override
	public AAIBB toAAIBB() {
		return intBox;
	}

	@Override
	public boolean contains(int x, int y, int z) {
		return intBox.contains(x, y, z);
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(start, end);
	}

	@Override
	public ZoneShape move(double x, double y, double z) {
		int ix = Mth.floor(x);
		int iy = Mth.floor(y);
		int iz = Mth.floor(z);

		if (ix == x && iy == y && iz == z) {
			return of(start.offset(ix, iy, iz), end.offset(ix, iy, iz));
		}

		return ZoneShape.super.move(x, y, z);
	}
}
