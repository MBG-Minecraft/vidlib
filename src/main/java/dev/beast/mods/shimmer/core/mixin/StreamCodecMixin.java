package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerStreamCodec;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StreamCodec.class)
public interface StreamCodecMixin<B, V> extends ShimmerStreamCodec<B, V> {
}
