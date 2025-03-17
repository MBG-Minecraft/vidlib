package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPalette(BlockState state, ShortList positions) {
	public static final Codec<BlockPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockState.CODEC.fieldOf("state").forGetter(BlockPalette::state),
		ShimmerCodecs.SHORT_LIST.fieldOf("positions").forGetter(BlockPalette::positions)
	).apply(instance, BlockPalette::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockPalette> STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.BLOCK_STATE, BlockPalette::state,
		ShimmerStreamCodecs.SHORT_LIST, BlockPalette::positions,
		BlockPalette::new
	);
}
