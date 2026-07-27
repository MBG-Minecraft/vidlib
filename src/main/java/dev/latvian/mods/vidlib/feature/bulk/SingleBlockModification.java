package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.block.PositionedBlock;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.DynamicType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record SingleBlockModification(PositionedBlock block) implements BulkLevelModification {
	public static final DynamicType<ByteBuf, BulkLevelModification> TYPE = DynamicType.create(
		"block",
		RecordCodecBuilder.<SingleBlockModification>mapCodec(instance -> instance.group(
			PositionedBlock.MAP_CODEC.forGetter(SingleBlockModification::block)
		).apply(instance, SingleBlockModification::new)),
		CompositeStreamCodec.of(
			BlockPos.STREAM_CODEC, p -> p.block.pos(),
			MCStreamCodecs.BLOCK_STATE, p -> p.block.state(),
			SingleBlockModification::new
		)
	);

	public SingleBlockModification(BlockPos pos, BlockState state) {
		this(new PositionedBlock(pos, state));
	}

	@Override
	public CustomRegistryType<ByteBuf, BulkLevelModification> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		sections.add(SectionPos.of(block.pos()));
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		blocks.set(block.pos(), block.state());
	}
}
