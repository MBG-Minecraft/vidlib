package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import imgui.ImGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockNotFilter(Ref<BlockFilter> filter) implements BlockFilter, ImBuilderWithHolder.Factory {
	public static CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> TYPE = REGISTRY.dynamic(
		ID.vidlib("not"),
		RecordCodecBuilder.mapCodec(i -> i.group(
			BlockFilter.CODEC.fieldOf("filter").forGetter(BlockNotFilter::filter)
		).apply(i, BlockNotFilter::new)),
		BlockFilter.STREAM_CODEC.map(BlockNotFilter::new, BlockNotFilter::filter)
	);

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = ImBuilderHolder.of("NOT", Builder::new);

		public final ImBuilder<BlockFilter> filter = BlockFilterImBuilder.create();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockNotFilter f) {
				filter.set(f.filter.value());
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.pushID("###not");
			ImGui.indent();
			var update = filter.imgui(graphics);
			ImGui.unindent();
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
	public CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return !filter.value().test(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return !filter.value().test(level, pos, state);
	}

	@Override
	public BlockFilter not() {
		return filter.value();
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
