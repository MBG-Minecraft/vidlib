package dev.latvian.mods.vidlib.core.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
	@WrapOperation(method = "createRoots", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
	private static ImmutableMap<ModelLayerLocation, LayerDefinition> vl$build(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> instance, Operation<ImmutableMap<ModelLayerLocation, LayerDefinition>> original) {
		return instance.buildKeepingLast();
	}
}
