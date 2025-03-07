package dev.beast.mods.shimmer.feature.block.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Function;
import java.util.function.Predicate;

public interface BlockFilter extends Predicate<BlockInWorld> {
	SimpleRegistry<BlockFilter> REGISTRY = SimpleRegistry.create(BlockFilter::type);

	SimpleRegistryType.Unit<BlockFilter> NONE = SimpleRegistryType.unit(Shimmer.id("none"), block -> false);
	SimpleRegistryType.Unit<BlockFilter> ANY = SimpleRegistryType.unit(Shimmer.id("any"), block -> true);

	static BlockFilter of(boolean value) {
		return value ? ANY.instance() : NONE.instance();
	}

	Codec<BlockFilter> CODEC = Codec.either(Codec.BOOL, REGISTRY.valueCodec()).xmap(either -> either.map(BlockFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	StreamCodec<RegistryFriendlyByteBuf, BlockFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.valueStreamCodec()).map(either -> either.map(BlockFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));

	static void bootstrap() {
		REGISTRY.register(NONE);
		REGISTRY.register(ANY);

		REGISTRY.register(BlockIdFilter.TYPE);
		REGISTRY.register(BlockStateFilter.TYPE);
		REGISTRY.register(BlockTagFilter.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}
}
