package dev.latvian.mods.vidlib.core.mixin.client;

import net.minecraft.client.renderer.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 26.3 replaced {@code net.minecraft.client.renderer.block.LiquidBlockRenderer} with
 * {@link FluidRenderer}, which no longer uses a raw {@code lightCoords & 15} wrap: the light
 * coordinates are packed through {@code LightCoordsUtil}. The old "wrap the light value passed
 * to the vertex pipeline" hook therefore has nothing to hook into, and this mixin is retained
 * (still registered in {@code vidlib.mixins.json}) as a documented placeholder so the hook point
 * is not silently lost.
 */
@Mixin(FluidRenderer.class)
public class LiquidBlockRendererMixin {
}
