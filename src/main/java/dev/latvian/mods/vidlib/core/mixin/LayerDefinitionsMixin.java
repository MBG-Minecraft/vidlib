package dev.latvian.mods.vidlib.core.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
	@Redirect(method = "createRoots", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
	private static ImmutableMap<ModelLayerLocation, LayerDefinition> vl$build(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> instance) {
		return instance.buildKeepingLast();
	}
}
