package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.geo.GeoPropRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GeoDisplayPropRenderer extends GeoPropRenderer<GeoDisplayProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(GeoDisplayProp.TYPE, new GeoDisplayPropRenderer());

	public static final DataTicket<ResourceLocation> MODEL = DataTicket.create("model", ResourceLocation.class);
	public static final DataTicket<ResourceLocation> TEXTURE = DataTicket.create("texture", ResourceLocation.class);

	public GeoDisplayPropRenderer() {
		super(new GeoDisplayPropModel());
	}

	@Override
	public void addRenderData(GeoDisplayProp prop, Void relatedObject, GeoRenderState state) {
		super.addRenderData(prop, relatedObject, state);
		state.addGeckolibData(MODEL, prop.model);
		state.addGeckolibData(TEXTURE, prop.texture);
	}

	@Override
	public void adjustPositionForRender(GeoRenderState state, PoseStack ms, BakedGeoModel model, boolean isReRender) {
		super.adjustPositionForRender(state, ms, model, isReRender);
	}

	@Override
	public void scaleModelForRender(GeoRenderState state, float widthScale, float heightScale, PoseStack poseStack, BakedGeoModel model, boolean isReRender) {
		super.scaleModelForRender(state, widthScale, heightScale, poseStack, model, isReRender);

		if (!isReRender) {
			var s = state.getGeckolibData(HEIGHT);

			if (s != null && s != 1F) {
				poseStack.scale(s, s, s);
			}
		}
	}
}
