package dev.beast.mods.shimmer.feature.clock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.beast.mods.shimmer.math.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ClockRenderer {
	private static final Color RED = new Color(1F, 1F, 0.3F, 0.3F);

	public static void render(Minecraft mc, ClockInstance instance, ClockLocation location, PoseStack ms, Vec3 cameraPos, float delta) {
		var font = location.font();

		var time = Mth.lerp(delta, instance.prevTick, instance.tick);
		var timeRemaining = instance.clock.maxTicks() - time;

		var itime = (int) time;
		var text = location.format().formatted(itime / 1200, (itime / 20) % 60).toCharArray();
		var width = font.getWidth(text);

		if (width <= 0) {
			return;
		}

		var light = location.fullbright() ? LightTexture.FULL_BRIGHT : LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, location.pos()), mc.level.getBrightness(LightLayer.SKY, location.pos()));

		ms.pushPose();
		ms.translate(location.pos().getX() + 0.5D - cameraPos.x, location.pos().getY() + 0.5D - cameraPos.y, location.pos().getZ() + 0.5D - cameraPos.z);
		ms.mulPose(Axis.YP.rotationDegrees(-location.rotation().toYRot()));

		ms.scale(1F, -1F, -1F);
		var m4 = ms.last().pose();
		var normal = new Vector3f(0F, -1F, 0F).mul(ms.last().normal());

		var buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(font.texture()));
		var x = -width / 2F + 1F;
		var y = -font.size().h() / 2F;
		var z = 0.4F;

		float red = 0F;

		if (instance.clock.flash() > 0) {
			if (timeRemaining < 20F) {
				red = 1F;
			} else if (timeRemaining < instance.clock.flash()) {
				red = 0.65F + Mth.cos((time - (instance.clock.maxTicks() - instance.clock.flash())) * 0.85F) * 0.35F;
			}
		}

		var color = location.color().lerp(red, RED);

		int cr = color.red();
		int cg = color.green();
		int cb = color.blue();
		int ca = 255;

		for (var c : text) {
			int index = c >= '0' && c <= '9' ? (c - '0') : 10;
			float u0 = font.uvs()[index].u0();
			float v0 = font.uvs()[index].v0();
			float u1 = font.uvs()[index].u1();
			float v1 = font.uvs()[index].v1();
			float cw = index == 10 ? font.actualSeparatorWidth() : font.size().w();
			float ch = font.size().h();

			buffer.addVertex(m4, x, y, z).setColor(cr, cg, cb, ca).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x, normal.y, normal.z);
			buffer.addVertex(m4, x + cw, y, z).setColor(cr, cg, cb, ca).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x, normal.y, normal.z);
			buffer.addVertex(m4, x + cw, y + ch, z).setColor(cr, cg, cb, ca).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x, normal.y, normal.z);
			buffer.addVertex(m4, x, y + ch, z).setColor(cr, cg, cb, ca).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x, normal.y, normal.z);

			x += cw + 1F;
		}

		ms.popPose();
	}
}
