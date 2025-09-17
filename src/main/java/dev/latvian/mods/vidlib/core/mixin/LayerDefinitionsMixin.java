package dev.latvian.mods.vidlib.core.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
	@ModifyExpressionValue(method = "createRoots", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
	private static ImmutableMap<ModelLayerLocation, LayerDefinition> modifyCreateRoots(ImmutableMap<ModelLayerLocation, LayerDefinition> original) {
		return MiscClientUtils.customLayerDefinitions(original);
	}
}
