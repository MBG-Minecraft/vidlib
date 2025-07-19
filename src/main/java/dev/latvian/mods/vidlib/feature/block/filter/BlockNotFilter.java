package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockNotFilter(BlockFilter filter) implements BlockFilter {
	public static SimpleRegistryType<BlockNotFilter> TYPE = SimpleRegistryType.dynamic("not", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.fieldOf("filter").forGetter(BlockNotFilter::filter)
	).apply(instance, BlockNotFilter::new)), BlockFilter.STREAM_CODEC.map(BlockNotFilter::new, BlockNotFilter::filter));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = new ImBuilderHolder<>("NOT", Builder::new);

		public final ImBuilder<BlockFilter> filter = BlockFilterImBuilder.create();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.pushID("###not");
			var update = filter.imgui(graphics);
			ImGui.popID();
			return update;
		}

		@Override
		public boolean isValid() {
			return filter.isValid();
		}

		@Override
		public BlockFilter build() {
			return filter.build().not();
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return !filter.test(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return !filter.test(level, pos, state);
	}

	@Override
	public BlockFilter not() {
		return filter;
	}
}
