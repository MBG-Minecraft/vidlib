package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.builder.TagKeyImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockTypeTagFilter(TagKey<Block> tag) implements BlockFilter, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<BlockTypeTagFilter> TYPE = SimpleRegistryType.dynamic("type_tag", RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTypeTagFilter::tag)
	).apply(instance, BlockTypeTagFilter::new)), KLibStreamCodecs.tagKey(Registries.BLOCK).map(BlockTypeTagFilter::new, BlockTypeTagFilter::tag));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = new ImBuilderHolder<>("Type Tag", Builder::new);

		public final ImBuilder<TagKey<Block>> tag = TagKeyImBuilder.BLOCK_TYPE.get();

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockTypeTagFilter f) {
				tag.set(f.tag);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return tag.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return tag.isValid();
		}

		@Override
		public BlockFilter build() {
			return new BlockTypeTagFilter(tag.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld b) {
		return b.getState().is(tag);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.is(tag);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
