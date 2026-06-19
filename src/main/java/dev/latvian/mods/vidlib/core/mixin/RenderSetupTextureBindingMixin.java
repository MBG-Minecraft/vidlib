package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLRenderSetupTextureBinding;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding")
public interface RenderSetupTextureBindingMixin extends VLRenderSetupTextureBinding {
	@Override
	@Accessor("location")
	Identifier vl$getLocation();
}
