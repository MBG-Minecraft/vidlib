package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record BlockZone(BlockPos start, BlockPos end, AABB box) implements Zone {
	public static final ZoneType<BlockZone> TYPE = new ZoneType<>("block", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(BlockZone::start),
		BlockPos.CODEC.fieldOf("end").forGetter(BlockZone::end)
	).apply(instance, BlockZone::new)), StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		BlockZone::start,
		BlockPos.STREAM_CODEC,
		BlockZone::end,
		BlockZone::new
	));

	public BlockZone(BlockPos start, BlockPos end) {
		this(start, end, AABB.encapsulatingFullBlocks(start, end));
	}

	@Override
	public ZoneType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}
}
