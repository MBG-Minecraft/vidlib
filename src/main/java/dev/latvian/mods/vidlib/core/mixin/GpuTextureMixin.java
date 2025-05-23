package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.textures.GpuTexture;
import dev.latvian.mods.vidlib.core.VLGpuTexture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GpuTexture.class)
public class GpuTextureMixin implements VLGpuTexture {
}
