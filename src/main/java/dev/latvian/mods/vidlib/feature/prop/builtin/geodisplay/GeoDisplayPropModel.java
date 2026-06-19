package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import dev.latvian.mods.vidlib.integration.VidLibGeoDataTickets;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class GeoDisplayPropModel extends GeoModel<GeoDisplayProp> {
	@Override
	public Identifier getModelResource(GeoRenderState renderState) {
		return renderState.getGeckolibData(VidLibGeoDataTickets.MODEL);
	}

	@Override
	public Identifier getTextureResource(GeoRenderState renderState) {
		return renderState.getGeckolibData(VidLibGeoDataTickets.TEXTURE);
	}

	@Override
	public Identifier getAnimationResource(GeoDisplayProp animatable) {
		return animatable.animations;
	}
}
