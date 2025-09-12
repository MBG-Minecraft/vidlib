package dev.latvian.mods.vidlib.feature.prop.builtin.playerstatue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class PlayerStatuePropRenderer extends GeoPropRenderer<PlayerStatueProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(PlayerStatueProp.TYPE, new PlayerStatuePropRenderer());

	public static final DataTicket<Boolean> STONE = DataTicket.create("stone", Boolean.class);
	public static final DataTicket<ResourceLocation> SKIN = DataTicket.create("skin", ResourceLocation.class);
	public static final DataTicket<Float> HEAD_PITCH = DataTicket.create("head_pitch", Float.class);
	public RandomSource randomSource = RandomSource.create(0L);

	public PlayerStatuePropRenderer() {
		super(new DefaultedPropGeoModel<>(VidLib.id("player"), ID.mc("textures/entity/player/wide/steve.png")));
	}

	@Override
	public void addRenderData(PlayerStatueProp prop, Void relatedObject, GeoRenderState state) {
		super.addRenderData(prop, relatedObject, state);
		float delta = state.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1F);
		state.addGeckolibData(DataTickets.ENTITY_YAW, prop.getYaw(delta) + (prop.randomYaw == 0F ? 0F : randomSource.nextRange(prop.randomYaw)));
		state.addGeckolibData(STONE, prop.stone);

		float headPitch = prop.headPitch + (prop.randomHeadPitch == 0F ? 0F : randomSource.nextRange(prop.randomHeadPitch));
		state.addGeckolibData(HEAD_PITCH, headPitch == 0F ? null : headPitch);

		if (!prop.profile.getName().isEmpty() && !prop.profile.getId().equals(Util.NIL_UUID)) {
			state.addGeckolibData(SKIN, Minecraft.getInstance().getSkinManager().getInsecureSkin(prop.profile).texture());
		} else {
			state.addGeckolibData(SKIN, null);
		}
	}

	@Override
	public void render(PropRenderContext<PlayerStatueProp> ctx) {
		var p = ctx.prop();
		int count = Math.max(1, p.count);

		float radius = p.spreadRadius;
		randomSource = RandomSource.create(BlockPos.asLong(Float.floatToIntBits((float) p.pos.x), count, Float.floatToIntBits((float) p.pos.z)));
		float spreadRandom = p.randomOffset;

		for (int i = 0; i < count; i++) {
			ctx.poseStack().pushPose();
			var offset = p.spread.offset(i, count, radius);
			ctx.poseStack().translate(offset.x(), 0F, offset.y());

			if (spreadRandom > 0F && count > 1) {
				ctx.poseStack().translate(randomSource.nextRange(spreadRandom), 0F, randomSource.nextRange(spreadRandom));
			}

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
	public void renderCubesOfBone(GeoRenderState state, GeoBone bone, PoseStack ms, VertexConsumer buffer, int packedLight, int packedOverlay, int renderColor) {
		var headPitch = state.getGeckolibData(HEAD_PITCH);

		if (headPitch != null && bone.getName().equals("head")) {
			ms.pushPose();
			ms.translate(0F, 1.5F, 0F);
			ms.mulPose(Axis.XN.rotationDegrees(headPitch));
			ms.translate(0F, -1.5F, 0F);
			super.renderCubesOfBone(state, bone, ms, buffer, packedLight, packedOverlay, renderColor);
			ms.popPose();
		} else {
			super.renderCubesOfBone(state, bone, ms, buffer, packedLight, packedOverlay, renderColor);
		}
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
