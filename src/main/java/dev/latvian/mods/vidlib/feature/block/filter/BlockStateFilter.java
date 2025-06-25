package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.block.ExactBlockStateImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockStateFilter(BlockState blockState) implements BlockFilter {
	public static final SimpleRegistryType<BlockStateFilter> TYPE = SimpleRegistryType.dynamic("block_state", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.BLOCK_STATE.fieldOf("block_state").forGetter(BlockStateFilter::blockState)
	).apply(instance, BlockStateFilter::new)), MCStreamCodecs.BLOCK_STATE.map(BlockStateFilter::new, BlockStateFilter::blockState));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = new ImBuilderHolder<>("Exact Block State", Builder::new);

		public final ExactBlockStateImBuilder builder = new ExactBlockStateImBuilder(null);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return builder.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return builder.isValid();
		}

		@Override
		public BlockFilter build() {
			return new BlockStateFilter(builder.build());
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
}
