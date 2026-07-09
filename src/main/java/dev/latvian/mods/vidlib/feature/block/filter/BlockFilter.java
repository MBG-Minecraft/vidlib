package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RefOptimizer;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLBlockInWorld;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public interface BlockFilter extends Predicate<BlockInWorld>, RefOptimizer<BlockFilter> {
	CustomRegistry<RegistryFriendlyByteBuf, BlockFilter> REGISTRY = CustomRegistry.<RegistryFriendlyByteBuf, BlockFilter>builder()
		.keys(ID.vidlib("block_filter"), VidLib.ID)
		.type(BlockFilter::type)
		.customCodec(KLibCodecs.or(
			Codec.BOOL.flatXmap(b -> DataResult.success(of(b)), filter -> {
				if (filter == of(true)) {
					return DataResult.success(true);
				} else if (filter == of(false)) {
					return DataResult.success(false);
				} else {
					return DataResult.error(() -> "Expected either 'any' or 'none'");
				}
			}),
			Codec.STRING.flatXmap(s -> {
				try {
					var state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, s, false).blockState();

					if (!state.isAir()) {
						if (s.indexOf('[') != -1) {
							return DataResult.success(new BlockStateFilter(state));
						} else {
							return DataResult.success(new BlockIdFilter(state.getBlock()));
						}
					}
				} catch (Exception ignore) {
				}

				return DataResult.error(() -> "Invalid blockstate format: " + s);
			}, filter -> switch (filter) {
				case BlockStateFilter f -> DataResult.success(f.blockState().vl$toString() + (f.blockState() == f.blockState().getBlock().defaultBlockState() ? "[]" : ""));
				case BlockIdFilter f -> DataResult.success(f.block().builtInRegistryHolder().getKey().identifier().toString());
				case null, default -> DataResult.error(() -> "");
			})
		))
		.build();

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, BlockFilter> NONE = REGISTRY.unitWithType(ID.vidlib("none"), type -> new SimpleBlockFilter(type) {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return false;
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return false;
		}

		@Override
		public BlockFilter and(BlockFilter filter) {
			return this;
		}

		@Override
		public BlockFilter not() {
			return ANY.value();
		}
	});

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, BlockFilter> ANY = REGISTRY.unitWithType(ID.vidlib("any"), type -> new SimpleBlockFilter(type) {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return true;
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return true;
		}

		@Override
		public BlockFilter and(BlockFilter filter) {
			return filter;
		}

		@Override
		public BlockFilter not() {
			return NONE.value();
		}
	});

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, BlockFilter> VISIBLE = REGISTRY.unitWithType(ID.vidlib("visible"), type -> new SimpleBlockFilter(type) {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			var state = blockInWorld.getState();
			return state != null && state.isVisible();
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return state.isVisible();
		}
	});

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, BlockFilter> PARTIAL = REGISTRY.unitWithType(ID.vidlib("partial"), type -> new SimpleBlockFilter(type) {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			var state = blockInWorld.getState();
			return state != null && state.isPartial();
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return state.isPartial();
		}
	});

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, BlockFilter> EXPOSED = REGISTRY.unitWithType(ID.vidlib("exposed"), type -> new SimpleBlockFilter(type) {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return blockInWorld.getLevel() instanceof Level l && test(l, blockInWorld.getPos(), blockInWorld.getState());
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return !state.isAir() && level.isBlockExposed(pos.getX(), pos.getY(), pos.getZ(), new BlockPos.MutableBlockPos());
		}
	});

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, BlockFilter> FLUID = REGISTRY.unitWithType(ID.vidlib("fluid"), type -> new SimpleBlockFilter(type) {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return blockInWorld.getLevel() instanceof Level l && test(l, blockInWorld.getPos(), blockInWorld.getState());
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return !state.isAir() && state.liquid();
		}
	});

	static BlockFilter of(boolean value) {
		return value ? ANY.value() : NONE.value();
	}

	Codec<Ref<BlockFilter>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<BlockFilter>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<BlockFilter>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, BlockFilter> registry) {
		registry.register(NONE);
		registry.register(ANY);
		registry.register(VISIBLE);
		registry.register(PARTIAL);
		registry.register(EXPOSED);
		registry.register(FLUID);

		registry.register(BlockNotFilter.TYPE);
		registry.register(BlockAndFilter.TYPE);
		registry.register(BlockOrFilter.TYPE);
		registry.register(BlockXorFilter.TYPE);

		registry.register(BlockIdFilter.TYPE);
		registry.register(BlockStateFilter.TYPE);
		registry.register(BlockTypeTagFilter.TYPE);
	}

	@Nullable
	default CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type() {
		return null;
	}

	default Ref<BlockFilter> ref() {
		return REGISTRY.ref(this);
	}

	default boolean test(Level level, BlockPos pos, BlockState state) {
		return test(VLBlockInWorld.of(level, pos, state));
	}

	default BlockFilter not() {
		return new BlockNotFilter(ref());
	}

	default BlockFilter and(BlockFilter filter) {
		if (filter == of(true)) {
			return this;
		} else if (filter == of(false)) {
			return filter;
		} else {
			return new BlockAndFilter(List.of(ref(), filter.ref()));
		}
	}

	default BlockFilter or(BlockFilter filter) {
		if (filter == of(true)) {
			return filter;
		} else if (filter == of(false)) {
			return this;
		} else {
			return new BlockOrFilter(List.of(ref(), filter.ref()));
		}
	}
}
