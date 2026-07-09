package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
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

public record BlockXorFilter(Ref<BlockFilter> a, Ref<BlockFilter> b) implements BlockFilter, ImBuilderWithHolder.Factory {
	public static CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> TYPE = REGISTRY.dynamic(
		ID.vidlib("xor"),
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockFilter.CODEC.fieldOf("a").forGetter(BlockXorFilter::a),
			BlockFilter.CODEC.fieldOf("b").forGetter(BlockXorFilter::b)
		).apply(instance, BlockXorFilter::new)),
		CompositeStreamCodec.of(
			BlockFilter.STREAM_CODEC, BlockXorFilter::a,
			BlockFilter.STREAM_CODEC, BlockXorFilter::b,
			BlockXorFilter::new
		)
	);

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = ImBuilderHolder.of("XOR", Builder::new);

		public final ImBuilder<BlockFilter> a = BlockFilterImBuilder.create();
		public final ImBuilder<BlockFilter> b = BlockFilterImBuilder.create();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockXorFilter f) {
				a.set(f.a.value());
				b.set(f.b.value());
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.indent();
			update = update.or(a.imguiKey(graphics, "A", "a"));
			update = update.or(b.imguiKey(graphics, "B", "b"));
			ImGui.unindent();
			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public BlockFilter build() {
			return new BlockXorFilter(a.build().optimize().ref(), b.build().optimize().ref());
		}
	}

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return a.value().test(block) ^ b.value().test(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return a.value().test(level, pos, state) ^ b.value().test(level, pos, state);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
