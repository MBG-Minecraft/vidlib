package dev.latvian.mods.vidlib.feature.prop.builtin.skeleton;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.geo.DefaultedPropGeoModel;
import dev.latvian.mods.vidlib.feature.prop.geo.GeoPropRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SkeletonPropRenderer extends GeoPropRenderer<SkeletonProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(SkeletonProp.TYPE, new SkeletonPropRenderer());

	public SkeletonPropRenderer() {
		super(new DefaultedPropGeoModel<>(VidLib.id("skeleton"), ID.mc("textures/entity/skeleton/skeleton.png")));
	}

	@Override
	public void adjustPositionForRender(GeoRenderState state, PoseStack ms, BakedGeoModel model, boolean isReRender) {
		if (!isReRender) {
			var s0 = state.getGeckolibData(HEIGHT);
			var p0 = state.getGeckolibData(DataTickets.ENTITY_PITCH);

			if (s0 != null && p0 != null) {
				ms.translate(0F, (float) KMath.map(p0, 0D, -90D, 0D, 0.13D * (s0 / 2D)), 0F);
			}
		}

		super.adjustPositionForRender(state, ms, model, isReRender);

		if (!isReRender) {
			var s0 = state.getGeckolibData(HEIGHT);
			var p0 = state.getGeckolibData(DataTickets.ENTITY_PITCH);

			if (s0 != null && p0 != null) {
				ms.translate(0F, (float) KMath.map(p0, 0D, -90D, 0D, 0.1D * (s0 / 2D)), 0F);
			}
		}
	}

	@Override
	public void scaleModelForRender(GeoRenderState state, float widthScale, float heightScale, PoseStack poseStack, BakedGeoModel model, boolean isReRender) {
		super.scaleModelForRender(state, widthScale, heightScale, poseStack, model, isReRender);

		if (!isReRender) {
			var s0 = state.getGeckolibData(HEIGHT);

			if (s0 != null) {
				float s = s0 / 2F;
				poseStack.scale(s, s, s);
			}
		}
	}

	@Override
	public void render(PropRenderContext<SkeletonProp> ctx) {
		super.render(ctx);

		// render name //
	}
}
