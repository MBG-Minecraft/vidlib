package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record BlockZoneShape(BlockPos start, BlockPos end, AABB box) implements ZoneShape {
	public static final ZoneShapeType<BlockZoneShape> TYPE = new ZoneShapeType<>("block", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(BlockZoneShape::start),
		BlockPos.CODEC.fieldOf("end").forGetter(BlockZoneShape::end)
	).apply(instance, BlockZoneShape::new)), StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		BlockZoneShape::start,
		BlockPos.STREAM_CODEC,
		BlockZoneShape::end,
		BlockZoneShape::new
	));

	public BlockZoneShape(BlockPos start, BlockPos end) {
		this(start, end, AABB.encapsulatingFullBlocks(start, end));
	}

	@Override
	public ZoneShapeType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}
}
