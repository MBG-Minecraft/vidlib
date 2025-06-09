package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

public record PositionedBlock(BlockPos pos, BlockState state) implements BulkLevelModification {
	public static final MapCodec<PositionedBlock> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("pos").forGetter(PositionedBlock::pos),
		MCCodecs.BLOCK_STATE.fieldOf("state").forGetter(PositionedBlock::state)
	).apply(instance, PositionedBlock::new));

	public static final Codec<PositionedBlock> CODEC = MAP_CODEC.codec();

	public static final StreamCodec<RegistryFriendlyByteBuf, PositionedBlock> STREAM_CODEC = CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, PositionedBlock::pos,
		MCStreamCodecs.BLOCK_STATE, PositionedBlock::state,
		PositionedBlock::new
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<PositionedBlock>> LIST_STREAM_CODEC = STREAM_CODEC.listOf();

	public static final SimpleRegistryType<PositionedBlock> TYPE = SimpleRegistryType.dynamic("block", MAP_CODEC, STREAM_CODEC);

	public static final DataType<PositionedBlock> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, PositionedBlock.class);
	public static final DataType<List<PositionedBlock>> LIST_DATA_TYPE = DATA_TYPE.listOf();

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

	@Override
	public String toString() {
		return "PositionedBlock[%d,%d,%d,%s]".formatted(pos.getX(), pos.getY(), pos.getZ(), state.vl$toString());
	}
}
