package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.feature.prop.geo.GeoPropRenderer;
import dev.latvian.mods.vidlib.integration.VidLibGeoDataTickets;
import net.minecraft.client.Minecraft;

public class GeoDisplayPropRenderer extends GeoPropRenderer<GeoDisplayProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = PropRenderer.holder(GeoDisplayProp.TYPE, new GeoDisplayPropRenderer());

	public GeoDisplayPropRenderer() {
		super(new GeoDisplayPropModel());
	}

	@Override
	public void extractRenderState(Minecraft mc, GeoDisplayProp prop, GeoRenderState state, float delta) {
		super.extractRenderState(mc, prop, state, delta);
		state.addGeckolibData(VidLibGeoDataTickets.MODEL, prop.model);
		state.addGeckolibData(VidLibGeoDataTickets.TEXTURE, prop.texture.texturePath());
	}

	@Override
	public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
		super.adjustRenderPose(renderPassInfo);
	}

	@Override
	public void scaleModelForRender(RenderPassInfo<GeoRenderState> renderPassInfo, float widthScale, float heightScale) {
		super.scaleModelForRender(renderPassInfo, widthScale, heightScale);

		var s = renderPassInfo.getGeckolibData(VidLibGeoDataTickets.HEIGHT);

		if (s != null && s != 1F) {
			renderPassInfo.poseStack().scale(s, s, s);
		}
	}
}
