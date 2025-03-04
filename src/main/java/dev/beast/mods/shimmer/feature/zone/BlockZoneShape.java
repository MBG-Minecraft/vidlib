package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.AAIBB;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public record BlockZoneShape(BlockPos start, BlockPos end, AABB box) implements ZoneShape {
	public static final SimpleRegistryType<BlockZoneShape> TYPE = SimpleRegistryType.dynamic(Shimmer.id("block"), RecordCodecBuilder.mapCodec(instance -> instance.group(
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
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(start, end);
	}

	public AAIBB toAAIBB() {
		return new AAIBB(start, end);
	}
}
