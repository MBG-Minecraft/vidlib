package dev.latvian.mods.vidlib.feature.prop.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.integration.VidLibGeoDataTickets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

public class GeoPropRenderer<P extends Prop & GeoProp> extends GeoObjectRenderer<P, Void, GeoRenderState> implements PropRenderer<P> {
	public GeoPropRenderer(GeoModel<P> model) {
		super(model);
	}

	public GeoPropRenderer(Identifier id) {
		super(new DefaultedPropGeoModel<>(id));
	}

	@Override
	public void render(PropRenderContext<P> ctx) {
		var submitNodeCollector = ctx.frame().submitNodeCollector();

		if (submitNodeCollector != null) {
			performRenderPass(ctx.prop(), null, ctx.poseStack(), submitNodeCollector, ctx.frame().cameraState(), getPackedLight(ctx), ctx.delta());
		}
	}

	public static float getDelta(GeoRenderState state) {
		var delta = state.getGeckolibData(DataTickets.PARTIAL_TICK);
		return delta == null ? 1F : delta;
	}

	@Override
	public final void addRenderData(P prop, Void relatedObject, GeoRenderState state, float delta) {
		extractRenderState(Minecraft.getInstance(), prop, state, delta);
	}

	public void extractRenderState(Minecraft mc, P prop, GeoRenderState state, float delta) {
		state.addGeckolibData(DataTickets.TICK, (double) prop.getTick(delta));
		state.addGeckolibData(DataTickets.ENTITY_PITCH, prop.getPitch(delta));
		state.addGeckolibData(DataTickets.ENTITY_YAW, prop.getYaw(delta));
		state.addGeckolibData(VidLibGeoDataTickets.ENTITY_ROLL, prop.getRoll(delta));
		// state.addGeckolibData(DataTickets.VELOCITY, new Vec3(prop.velocity.x, prop.velocity.y, prop.velocity.z));
		state.addGeckolibData(DataTickets.VELOCITY, Vec3.ZERO);
		var pos = prop.getPos(delta);
		state.addGeckolibData(DataTickets.BLOCKPOS, BlockPos.containing(pos));
		state.addGeckolibData(DataTickets.POSITION, pos);
		state.addGeckolibData(VidLibGeoDataTickets.WIDTH, (float) prop.width);
		state.addGeckolibData(VidLibGeoDataTickets.HEIGHT, (float) prop.height);
		state.addGeckolibData(VidLibGeoDataTickets.CAMERA_DISTANCE, pos.distanceTo(mc.gameRenderer.getMainCamera().position()));
	}

	@Override
	public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
		var state = renderPassInfo.renderState();
		var ms = renderPassInfo.poseStack();
		var roll = state.getGeckolibData(VidLibGeoDataTickets.ENTITY_ROLL);

		if (roll != null) {
			ms.mulPose(Axis.ZP.rotationDegrees(roll));
		}

		var yaw = state.getGeckolibData(DataTickets.ENTITY_YAW);

		if (yaw != null) {
			ms.mulPose(Axis.YP.rotationDegrees(yaw));
		}

		var pitch = state.getGeckolibData(DataTickets.ENTITY_PITCH);

		if (pitch != null) {
			ms.mulPose(Axis.XP.rotationDegrees(pitch));
		}
	}
}
