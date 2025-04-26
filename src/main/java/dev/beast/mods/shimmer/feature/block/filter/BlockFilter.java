package dev.beast.mods.shimmer.feature.block.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerBlockInWorld;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface BlockFilter extends Predicate<BlockInWorld> {
	SimpleRegistry<BlockFilter> REGISTRY = SimpleRegistry.create(BlockFilter::type);

	SimpleRegistryType.Unit<BlockFilter> NONE = SimpleRegistryType.unit(Shimmer.id("none"), new BlockFilter() {
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
			return "shimmer:none";
		}
	});

	SimpleRegistryType.Unit<BlockFilter> ANY = SimpleRegistryType.unit(Shimmer.id("any"), new BlockFilter() {
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
			return "shimmer:any";
		}
	});

	SimpleRegistryType.Unit<BlockFilter> VISIBLE = SimpleRegistryType.unit(Shimmer.id("visible"), new BlockFilter() {
		@Override
		public boolean test(BlockInWorld blockInWorld) {
			var state = blockInWorld.getState();
			return state != null && state.getRenderShape() != RenderShape.INVISIBLE;
		}

		@Override
		public boolean test(Level level, BlockPos pos, BlockState state) {
			return state.getRenderShape() != RenderShape.INVISIBLE;
		}

		@Override
		public String toString() {
			return "shimmer:visible";
		}
	});

	static BlockFilter of(boolean value) {
		return value ? ANY.instance() : NONE.instance();
	}

	Codec<BlockFilter> CODEC = Codec.either(Codec.BOOL, REGISTRY.valueCodec()).xmap(either -> either.map(BlockFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	StreamCodec<RegistryFriendlyByteBuf, BlockFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.valueStreamCodec()).map(either -> either.map(BlockFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	KnownCodec<BlockFilter> KNOWN_CODEC = KnownCodec.register(Shimmer.id("block_filter"), CODEC, STREAM_CODEC, BlockFilter.class);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(NONE);
		REGISTRY.register(ANY);
		REGISTRY.register(VISIBLE);

		REGISTRY.register(BlockNotFilter.TYPE);
		REGISTRY.register(BlockAndFilter.TYPE);
		REGISTRY.register(BlockOrFilter.TYPE);
		REGISTRY.register(BlockXorFilter.TYPE);

		REGISTRY.register(BlockIdFilter.TYPE);
		REGISTRY.register(BlockStateFilter.TYPE);
		REGISTRY.register(BlockTagFilter.TYPE);
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
			return test(ShimmerBlockInWorld.of(level, pos, state));
		}
	}

	default BlockFilter not() {
		return new BlockNotFilter(this);
	}

	default BlockFilter and(BlockFilter filter) {
		return new BlockAndFilter(List.of(this, filter));
	}
}
