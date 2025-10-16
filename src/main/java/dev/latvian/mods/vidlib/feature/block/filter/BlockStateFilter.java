package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.block.ExactBlockStateImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockStateFilter(BlockState blockState) implements BlockFilter, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<BlockStateFilter> TYPE = SimpleRegistryType.dynamic("block_state", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.BLOCK_STATE.fieldOf("block_state").forGetter(BlockStateFilter::blockState)
	).apply(instance, BlockStateFilter::new)), MCStreamCodecs.BLOCK_STATE.map(BlockStateFilter::new, BlockStateFilter::blockState));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = ImBuilderHolder.of("Exact Block State", Builder::new);

		public final ExactBlockStateImBuilder blockState = new ExactBlockStateImBuilder();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockStateFilter f) {
				blockState.set(f.blockState);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return blockState.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return blockState.isValid();
		}

		@Override
		public BlockFilter build() {
			return new BlockStateFilter(blockState.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return block.getState() == blockState;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state == blockState;
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
