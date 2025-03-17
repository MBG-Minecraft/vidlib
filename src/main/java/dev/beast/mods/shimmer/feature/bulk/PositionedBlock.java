package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record PositionedBlock(BlockPos pos, BlockState state) implements BulkLevelModification {
	public static final MapCodec<PositionedBlock> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("pos").forGetter(PositionedBlock::pos),
		BlockState.CODEC.fieldOf("state").forGetter(PositionedBlock::state)
	).apply(instance, PositionedBlock::new));

	public static final Codec<PositionedBlock> CODEC = MAP_CODEC.codec();

	public static final StreamCodec<RegistryFriendlyByteBuf, PositionedBlock> STREAM_CODEC = CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, PositionedBlock::pos,
		ShimmerStreamCodecs.BLOCK_STATE, PositionedBlock::state,
		PositionedBlock::new
	);

	public static final SimpleRegistryType<PositionedBlock> TYPE = SimpleRegistryType.dynamic(Shimmer.id("block"), MAP_CODEC, STREAM_CODEC);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		sections.add(SectionPos.of(pos));
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		blocks.set(pos, state);
	}
}
