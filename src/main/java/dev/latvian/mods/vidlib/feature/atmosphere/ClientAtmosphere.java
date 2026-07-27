package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.List;

public class ClientAtmosphere implements CustomSkyboxRenderer {
	public static final MenuItem MENU_ITEM = MenuItem.menu(ImIcons.SUN, "Atmosphere", g -> {
		if (!g.inGame) {
			return List.of();
		}

		var slist = new ArrayList<MenuItem>();

		var current = g.mc.level.getAtmosphere();
		var session = g.mc.player.vl$sessionData();

		for (var ref : Atmosphere.REGISTRY) {
			if (ref.optionalValue() == null) {
				continue;
			}

			var tex = session.getAtmosphere(ref).loadTexture(g.mc);

			slist.add(MenuItem.item(tex.getIcon(), ref.key(), current != null && current.key() == ref.key(), g1 -> {
				if (g1.isReplay || !g1.serverFeatures.has(Feature.ATMOSPHERE)) {
					g1.mc.getDataMap().setSuperOverride(InternalServerData.ATMOSPHERE, ref);
				} else {
					g1.mc.runClientCommand("atmosphere set \"" + ref.key() + "\"");
				}
			}));
		}

		slist.add(MenuItem.SEPARATOR);

		slist.add(MenuItem.item(ImIcons.INVISIBLE, "Vanilla", current == null, g1 -> {
			if (g1.isReplay || !g1.serverFeatures.has(Feature.ATMOSPHERE)) {
				g1.mc.getDataMap().setSuperOverride(InternalServerData.ATMOSPHERE, null);
			} else {
				g1.mc.runClientCommand("atmosphere remove");
			}
		}));

		return slist;
	}).remainOpen(true);

	public final String id;
	public final SkyboxTextureData skybox;
	public SkyboxTexture skyboxTexture;
	public final EnvironmentAttributeMap attributes;
	public final boolean sun;
	public final boolean moon;
	public final Rotation celestialRotation;

	public ClientAtmosphere(Ref<Atmosphere> ref) {
		this.id = ref.key();
		var data = ref.value();
		this.skybox = data.skybox().orElse(null);
		this.skyboxTexture = null;
		this.attributes = data.attributes();
		this.sun = data.sun().orElse(true);
		this.moon = data.moon().orElse(true);
		this.celestialRotation = data.celestialRotation().orElse(null);
	}

	public SkyboxTexture loadTexture(Minecraft mc) {
		if (skyboxTexture == null) {
			var texId = ID.vidlib("textures/vidlib/generated/skybox/" + id + ".png");

			if (mc.getTextureManager().byPath.get(texId) instanceof SkyboxTexture tex) {
				skyboxTexture = tex;
				return tex;
			}

			var tex = new SkyboxTexture(texId, this);
			mc.getTextureManager().registerAndLoad(tex.resourceId(), tex);
			skyboxTexture = tex;
			return tex;
		}

		return skyboxTexture;
	}

	@Override
	public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4fc modelViewMatrix, Runnable setupFog) {
		var mc = Minecraft.getInstance();
		// setupFog.run();
		// return renderSky(mc, levelRenderState, modelViewMatrix);
		skyRenderState.skyColor = 0x3D91A9;
		return false;
	}

	public boolean renderSky(Minecraft mc, LevelRenderState levelRenderState, Matrix4fc modelViewMatrix) {
		if (skybox == null) {
			return false;
		}

		float ps = levelRenderState.cameraRenderState.depthFar * 0.5F;
		float ns = -ps;

		var texture = loadTexture(mc);

		var ms = new PoseStack();
		// ms.mulPose(modelViewMatrix);
		ms.pushPose();

		if (skybox.rotating() != 0F) {
			float gameTime = mc.level == null ? 0F : (mc.level.getGameTime() % 24000L) / 24000F;
			ms.mulPose(Axis.YP.rotationDegrees(skybox.rotation() + 360F * gameTime * skybox.rotating()));
		}

		var buffer = mc.renderBuffers().bufferSource().getBuffer(VidLibRenderTypes.SKYBOX.apply(texture.resourceId()));
		var m = ms.last().pose();

		int cr = skybox.tint().red();
		int cg = skybox.tint().green();
		int cb = skybox.tint().blue();
		int ca = skybox.tint().alpha();

		// Up
		{
			float u0 = 0.25F;
			float v0 = 0F;
			float u1 = 0.5F;
			float v1 = 0.5F;
			buffer.addVertex(m, ns, ps, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
		}

		// Down
		{
			float u0 = 0.5F;
			float v0 = 0F;
			float u1 = 0.75F;
			float v1 = 0.5F;
			buffer.addVertex(m, ns, ns, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
		}

		float v0 = 0.5F;
		float v1 = 1F;

		// North
		{
			float u0 = 0.25F;
			float u1 = 0.5F;
			buffer.addVertex(m, ns, ns, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
		}

		// South
		{
			float u0 = 0.75F;
			float u1 = 1F;
			buffer.addVertex(m, ns, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
		}

		// West
		{
			float u0 = 0F;
			float u1 = 0.25F;
			buffer.addVertex(m, ns, ns, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
		}

		// East
		{
			float u0 = 0.5F;
			float u1 = 0.75F;
			buffer.addVertex(m, ps, ns, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
		}

		ms.popPose();
		return false;
	}
}
