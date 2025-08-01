package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockXorFilter(BlockFilter a, BlockFilter b) implements BlockFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<BlockXorFilter> TYPE = SimpleRegistryType.dynamic("xor", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.fieldOf("a").forGetter(BlockXorFilter::a),
		BlockFilter.CODEC.fieldOf("b").forGetter(BlockXorFilter::b)
	).apply(instance, BlockXorFilter::new)), CompositeStreamCodec.of(
		BlockFilter.STREAM_CODEC, BlockXorFilter::a,
		BlockFilter.STREAM_CODEC, BlockXorFilter::b,
		BlockXorFilter::new
	));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = new ImBuilderHolder<>("XOR", Builder::new);

		public final ImBuilder<BlockFilter> a = BlockFilterImBuilder.create();
		public final ImBuilder<BlockFilter> b = BlockFilterImBuilder.create();

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockXorFilter f) {
				a.set(f.a);
				b.set(f.b);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(a.imguiKey(graphics, "A", "a"));
			update = update.or(b.imguiKey(graphics, "B", "b"));
			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public BlockFilter build() {
			return new BlockXorFilter(a.build(), b.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return a.test(block) ^ b.test(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return a.test(level, pos, state) ^ b.test(level, pos, state);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
