package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLStreamCodec;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StreamCodec.class)
public interface StreamCodecMixin<B, V> extends VLStreamCodec<B, V> {
}
