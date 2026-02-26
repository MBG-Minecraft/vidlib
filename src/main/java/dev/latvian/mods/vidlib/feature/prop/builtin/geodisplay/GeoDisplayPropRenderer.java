package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.geo.GeoPropRenderer;
import dev.latvian.mods.vidlib.integration.VidLibGeoDataTickets;
import net.minecraft.client.Minecraft;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GeoDisplayPropRenderer extends GeoPropRenderer<GeoDisplayProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(GeoDisplayProp.TYPE, new GeoDisplayPropRenderer());

	public GeoDisplayPropRenderer() {
		super(new GeoDisplayPropModel());
	}

	@Override
	public void extractRenderState(Minecraft mc, GeoDisplayProp prop, GeoRenderState state, float delta) {
		super.extractRenderState(mc, prop, state, delta);
		state.addGeckolibData(VidLibGeoDataTickets.MODEL, prop.model);
		state.addGeckolibData(VidLibGeoDataTickets.TEXTURE, prop.texture);
	}

	@Override
	public void adjustPositionForRender(GeoRenderState state, PoseStack ms, BakedGeoModel model, boolean isReRender) {
		super.adjustPositionForRender(state, ms, model, isReRender);
	}

	@Override
	public void scaleModelForRender(GeoRenderState state, float widthScale, float heightScale, PoseStack poseStack, BakedGeoModel model, boolean isReRender) {
		super.scaleModelForRender(state, widthScale, heightScale, poseStack, model, isReRender);

		if (!isReRender) {
			var s = state.getGeckolibData(VidLibGeoDataTickets.HEIGHT);

			if (s != null && s != 1F) {
				poseStack.scale(s, s, s);
			}
		}
	}
}
