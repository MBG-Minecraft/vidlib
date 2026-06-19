package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLRenderSetup;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderSetup.class)
public interface RenderSetupMixin extends VLRenderSetup {
	@Override
	@Accessor("textures")
	Map<String, ?> vl$getTextures();
}
