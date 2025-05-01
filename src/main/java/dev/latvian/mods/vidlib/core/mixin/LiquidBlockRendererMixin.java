package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
	@ModifyConstant(method = "tesselate", constant = @Constant(intValue = 15))
	private int shimmer$blockWrap(int value, @Local(argsOnly = true) VertexConsumer buffer) {
		return buffer instanceof StructureRenderer.FluidTransformingVertexPipeline ? Integer.MAX_VALUE : value;
	}
}
