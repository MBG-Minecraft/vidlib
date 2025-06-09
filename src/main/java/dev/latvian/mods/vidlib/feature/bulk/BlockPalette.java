package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CollectionCodecs;
import dev.latvian.mods.klib.codec.CollectionStreamCodecs;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPalette(BlockState state, ShortList positions) {
	public static final Codec<BlockPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MCCodecs.BLOCK_STATE.fieldOf("state").forGetter(BlockPalette::state),
		CollectionCodecs.SHORT_LIST.fieldOf("positions").forGetter(BlockPalette::positions)
	).apply(instance, BlockPalette::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockPalette> STREAM_CODEC = CompositeStreamCodec.of(
		MCStreamCodecs.BLOCK_STATE, BlockPalette::state,
		CollectionStreamCodecs.SHORT_LIST, BlockPalette::positions,
		BlockPalette::new
	);
}
