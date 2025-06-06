package dev.latvian.mods.vidlib.feature.prop.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GeoPropRenderer<P extends Prop & GeoProp> extends GeoObjectRenderer<P> implements PropRenderer<P> {
	public static final DataTicket<Float> ENTITY_ROLL = DataTicket.create("entity_roll", Float.class);

	public GeoPropRenderer(GeoModel<P> model) {
		super(model);
	}

	@Override
	public void renderProp(P prop, FrameInfo frame) {
		int light = getPackedLight(prop);
		render(frame.poseStack(), prop, frame.buffers(), null, null, light, frame.worldDelta());
	}

	@Override
	public void addRenderData(P prop, Void relatedObject, GeoRenderState state) {
		var delta = state.getGeckolibData(DataTickets.PARTIAL_TICK);

		if (delta != null) {
			state.addGeckolibData(DataTickets.ENTITY_PITCH, prop.getPitch(delta));
			state.addGeckolibData(DataTickets.ENTITY_YAW, prop.getYaw(delta));
			state.addGeckolibData(ENTITY_ROLL, prop.getRoll(delta));
			state.addGeckolibData(DataTickets.VELOCITY, new Vec3(prop.velocity.x, prop.velocity.y, prop.velocity.z));
			var pos = prop.getPos(delta);
			state.addGeckolibData(DataTickets.BLOCKPOS, BlockPos.containing(pos));
			state.addGeckolibData(DataTickets.POSITION, pos);
		}
	}

	@Override
	public void adjustPositionForRender(GeoRenderState state, PoseStack ms, BakedGeoModel model, boolean isReRender) {
		if (!isReRender) {
			var yaw = state.getGeckolibData(DataTickets.ENTITY_YAW);

			if (yaw != null) {
				ms.mulPose(Axis.YP.rotationDegrees(yaw));
			}

			var pitch = state.getGeckolibData(DataTickets.ENTITY_PITCH);

			if (pitch != null) {
				ms.mulPose(Axis.XP.rotationDegrees(pitch));
			}

			var roll = state.getGeckolibData(ENTITY_ROLL);

			if (roll != null) {
				ms.mulPose(Axis.ZP.rotationDegrees(roll));
			}
		}
	}
}
