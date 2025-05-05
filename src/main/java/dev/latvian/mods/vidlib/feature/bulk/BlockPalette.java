package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPalette(BlockState state, ShortList positions) {
	public static final Codec<BlockPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		VLCodecs.BLOCK_STATE.fieldOf("state").forGetter(BlockPalette::state),
		VLCodecs.SHORT_LIST.fieldOf("positions").forGetter(BlockPalette::positions)
	).apply(instance, BlockPalette::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockPalette> STREAM_CODEC = CompositeStreamCodec.of(
		VLStreamCodecs.BLOCK_STATE, BlockPalette::state,
		VLStreamCodecs.SHORT_LIST, BlockPalette::positions,
		BlockPalette::new
	);
}
