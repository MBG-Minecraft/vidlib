package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GeoDisplayPropModel extends GeoModel<GeoDisplayProp> {
	@Override
	public ResourceLocation getModelResource(GeoRenderState renderState) {
		return renderState.getGeckolibData(GeoDisplayPropRenderer.MODEL);
	}

	@Override
	public ResourceLocation getTextureResource(GeoRenderState renderState) {
		return renderState.getGeckolibData(GeoDisplayPropRenderer.TEXTURE);
	}

	@Override
	public ResourceLocation getAnimationResource(GeoDisplayProp animatable) {
		return animatable.animations;
	}
}
