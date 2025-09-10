package dev.latvian.mods.vidlib.feature.prop.builtin.playerstatue;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.geo.DefaultedPropGeoModel;
import dev.latvian.mods.vidlib.feature.prop.geo.GeoPropRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class PlayerStatuePropRenderer extends GeoPropRenderer<PlayerStatueProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(PlayerStatueProp.TYPE, new PlayerStatuePropRenderer());

	public static final DataTicket<Boolean> STONE = DataTicket.create("stone", Boolean.class);
	public static final DataTicket<ResourceLocation> SKIN = DataTicket.create("skin", ResourceLocation.class);

	public PlayerStatuePropRenderer() {
		super(new DefaultedPropGeoModel<>(VidLib.id("player"), ID.mc("textures/entity/player/wide/steve.png")));
	}

	@Override
	public void addRenderData(PlayerStatueProp prop, Void relatedObject, GeoRenderState state) {
		super.addRenderData(prop, relatedObject, state);
		state.addGeckolibData(STONE, prop.stone);

		if (!prop.profile.getName().isEmpty() && !prop.profile.getId().equals(Util.NIL_UUID)) {
			state.addGeckolibData(SKIN, Minecraft.getInstance().getSkinManager().getInsecureSkin(prop.profile).texture());
		} else {
			state.addGeckolibData(SKIN, null);
		}
	}

	@Override
	public void render(PropRenderContext<PlayerStatueProp> ctx) {
		int count = Math.max(1, ctx.prop().count);

		if (count == 1) {
			super.render(ctx);
			return;
		}

		float radius = ctx.prop().spreadRadius;
		var random = RandomSource.create(10L);
		float spreadRandom = ctx.prop().spreadRandom;

		for (int i = 0; i < count; i++) {
			ctx.poseStack().pushPose();
			var offset = ctx.prop().spread.offset(i, count, radius);
			ctx.poseStack().translate(offset.x() + random.nextRange(spreadRandom), 0F, offset.y() + random.nextRange(spreadRandom));
			super.render(ctx);
			ctx.poseStack().popPose();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(GeoRenderState state) {
		var skin = state.getGeckolibData(SKIN);

		if (skin != null) {
			return skin;
		}

		return super.getTextureLocation(state);
	}

	@Override
	@Nullable
	public RenderType getRenderType(GeoRenderState state, ResourceLocation texture) {
		if (Boolean.TRUE.equals(state.getGeckolibData(STONE))) {
			return EntityRenderTypes.STONE_CUTOUT_NO_CULL.apply(texture);
		}

		return super.getRenderType(state, texture);
	}

	@Override
	public void scaleModelForRender(GeoRenderState state, float widthScale, float heightScale, PoseStack poseStack, BakedGeoModel model, boolean isReRender) {
		super.scaleModelForRender(state, widthScale, heightScale, poseStack, model, isReRender);

		if (!isReRender) {
			var s0 = state.getGeckolibData(HEIGHT);

			if (s0 != null) {
				float s = s0 / 1.92F;
				poseStack.scale(s, s, s);
			}
		}
	}
}
