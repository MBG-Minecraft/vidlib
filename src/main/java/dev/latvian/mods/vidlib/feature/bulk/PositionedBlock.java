package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
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
		VLCodecs.BLOCK_STATE.fieldOf("state").forGetter(PositionedBlock::state)
	).apply(instance, PositionedBlock::new));

	public static final Codec<PositionedBlock> CODEC = MAP_CODEC.codec();

	public static final StreamCodec<RegistryFriendlyByteBuf, PositionedBlock> STREAM_CODEC = CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, PositionedBlock::pos,
		VLStreamCodecs.BLOCK_STATE, PositionedBlock::state,
		PositionedBlock::new
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<PositionedBlock>> LIST_STREAM_CODEC = STREAM_CODEC.list();

	public static final SimpleRegistryType<PositionedBlock> TYPE = SimpleRegistryType.dynamic("block", MAP_CODEC, STREAM_CODEC);

	public static final KnownCodec<PositionedBlock> KNOWN_CODEC = KnownCodec.register(VidLib.id("positioned_block"), CODEC, STREAM_CODEC, PositionedBlock.class);
	public static final KnownCodec<List<PositionedBlock>> LIST_KNOWN_CODEC = KNOWN_CODEC.listOf();

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
