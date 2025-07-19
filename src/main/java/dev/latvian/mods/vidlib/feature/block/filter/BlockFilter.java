package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.core.VLBlockInWorld;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolderList;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.util.StringUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface BlockFilter extends Predicate<BlockInWorld> {
	SimpleRegistry<BlockFilter> REGISTRY = SimpleRegistry.create(BlockFilter::type);
	ImBuilderHolderList<BlockFilter> IMGUI_BUILDERS = new ImBuilderHolderList<>();

	SimpleRegistryType.Unit<BlockFilter> NONE = SimpleRegistryType.unit("none", new BlockFilter() {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return false;
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return false;
		}

		@Override
		public String toString() {
			return "none";
		}

		@Override
		public BlockFilter and(BlockFilter filter) {
			return this;
		}
	});

	SimpleRegistryType.Unit<BlockFilter> ANY = SimpleRegistryType.unit("any", new BlockFilter() {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return true;
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return true;
		}

		@Override
		public String toString() {
			return "any";
		}

		@Override
		public BlockFilter and(BlockFilter filter) {
			return filter;
		}
	});

	SimpleRegistryType.Unit<BlockFilter> VISIBLE = SimpleRegistryType.unit("visible", new BlockFilter() {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			var state = blockInWorld.getState();
			return state != null && state.isVisible();
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return state.isVisible();
		}

		@Override
		public String toString() {
			return "visible";
		}
	});

	SimpleRegistryType.Unit<BlockFilter> EXPOSED = SimpleRegistryType.unit("exposed", new BlockFilter() {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			return blockInWorld.getLevel() instanceof Level l && test(l, blockInWorld.getPos(), blockInWorld.getState());
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return !state.isAir() && level.isBlockExposed(pos.getX(), pos.getY(), pos.getZ(), new BlockPos.MutableBlockPos());
		}

		@Override
		public String toString() {
			return "exposed";
		}
	});

	static BlockFilter of(boolean value) {
		return value ? ANY.instance() : NONE.instance();
	}

	Codec<BlockFilter> NONE_OR_ANY_CODEC = Codec.BOOL.flatXmap(b -> DataResult.success(of(b)), filter -> {
		if (filter == ANY.instance()) {
			return DataResult.success(true);
		} else if (filter == NONE.instance()) {
			return DataResult.success(false);
		} else {
			return DataResult.error(() -> "Expected either 'any' or 'none'");
		}
	});

	Codec<BlockFilter> LITERAL_CODEC = Codec.STRING.flatXmap(s -> {
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
		case BlockIdFilter f -> DataResult.success(f.block().builtInRegistryHolder().getKey().location().toString());
		case null, default -> DataResult.error(() -> "");
	});

	Codec<BlockFilter> CODEC = KLibCodecs.or(List.of(NONE_OR_ANY_CODEC, LITERAL_CODEC, REGISTRY.valueCodec()));
	StreamCodec<RegistryFriendlyByteBuf, BlockFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.valueStreamCodec()).map(either -> either.map(BlockFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	DataType<BlockFilter> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, BlockFilter.class);
	CommandDataType<BlockFilter> COMMAND = CommandDataType.of(DATA_TYPE);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(NONE);
		REGISTRY.register(ANY);
		REGISTRY.register(VISIBLE);
		REGISTRY.register(EXPOSED);

		REGISTRY.register(BlockNotFilter.TYPE);
		REGISTRY.register(BlockAndFilter.TYPE);
		REGISTRY.register(BlockOrFilter.TYPE);
		REGISTRY.register(BlockXorFilter.TYPE);

		REGISTRY.register(BlockIdFilter.TYPE);
		REGISTRY.register(BlockStateFilter.TYPE);
		REGISTRY.register(BlockTypeTagFilter.TYPE);

		for (var unit : REGISTRY.unitValueMap().entrySet()) {
			IMGUI_BUILDERS.addUnit(StringUtils.snakeCaseToTitleCase(unit.getKey()), unit.getValue());
		}

		IMGUI_BUILDERS.add(BlockNotFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(BlockAndFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(BlockOrFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(BlockXorFilter.Builder.TYPE);

		IMGUI_BUILDERS.add(BlockIdFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(BlockStateFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(BlockTypeTagFilter.Builder.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default boolean test(Level level, BlockPos pos, BlockState state) {
		if (this == NONE.instance()) {
			return false;
		} else if (this == ANY.instance()) {
			return true;
		} else {
			return test(VLBlockInWorld.of(level, pos, state));
		}
	}

	default BlockFilter not() {
		if (this == ANY.instance()) {
			return NONE.instance();
		} else if (this == NONE.instance()) {
			return ANY.instance();
		} else {
			return new BlockNotFilter(this);
		}
	}

	default BlockFilter and(BlockFilter filter) {
		if (filter == ANY.instance()) {
			return this;
		} else if (filter == NONE.instance()) {
			return filter;
		} else {
			return new BlockAndFilter(List.of(this, filter));
		}
	}
}
