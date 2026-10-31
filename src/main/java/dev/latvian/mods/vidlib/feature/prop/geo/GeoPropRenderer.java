package dev.latvian.mods.vidlib.feature.prop.geo;

import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.client.SubmitContext;
import dev.latvian.mods.vidlib.integration.VidLibGeoDataTickets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class GeoPropRenderer<P extends Prop & GeoProp, O> extends GeoObjectRenderer<P, O, GeoRenderState> implements PropRenderer<P> {
	public GeoPropRenderer(GeoModel<P> model) {
		super(model);
	}

	public GeoPropRenderer(Identifier id) {
		super(new DefaultedPropGeoModel<>(id));
	}

	@Override
	public void render(SubmitContext<P> ctx) {
		var mc = Minecraft.getInstance();
		var state = createRenderState(ctx.prop(), null);
		extractRenderState(mc, ctx.prop(), state, ctx.delta());

		var cameraState = new CameraRenderState();
		cameraState.pos = mc.gameRenderer.mainCamera().position();
		cameraState.initialized = true;

		performRenderPass(ctx.prop(), null, ctx.poseStack(), ctx.collector(), cameraState, getPackedLight(ctx), ctx.delta());
	}

	public static float getDelta(GeoRenderState state) {
		var delta = state.getGeckolibData(DataTickets.PARTIAL_TICK);
		return delta == null ? 1F : delta;
	}

	@Override
	public GeoRenderState createRenderState(P prop, O relatedObject) {
		return new GeoRenderState() {
		};
	}

	@Override
	public void addRenderData(P prop, O relatedObject, GeoRenderState state, float delta) {
		extractRenderState(Minecraft.getInstance(), prop, state, delta);
	}

	@Override
	public void setMolangQueryValues(P prop, O relatedObject, GeoRenderState state, float delta) {
	}

	public void extractRenderState(Minecraft mc, P prop, GeoRenderState state, float delta) {
		state.addGeckolibData(DataTickets.TICK, (double) prop.getTick(delta));
		state.addGeckolibData(DataTickets.ENTITY_PITCH, prop.getPitch(delta));
		state.addGeckolibData(DataTickets.ENTITY_YAW, prop.getYaw(delta));
		state.addGeckolibData(VidLibGeoDataTickets.ENTITY_ROLL, prop.getRoll(delta));
		var pos = prop.getPos(delta);
		state.addGeckolibData(DataTickets.BLOCKPOS, BlockPos.containing(pos));
		state.addGeckolibData(DataTickets.POSITION, pos);
		state.addGeckolibData(VidLibGeoDataTickets.WIDTH, (float) prop.width);
		state.addGeckolibData(VidLibGeoDataTickets.HEIGHT, (float) prop.height);
		state.addGeckolibData(VidLibGeoDataTickets.CAMERA_DISTANCE, pos.distanceTo(mc.gameRenderer.mainCamera().position()));
	}
}
