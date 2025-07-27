package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.math.Axis;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import imgui.type.ImBoolean;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;

public class ClockRenderer {
	public static final ImBoolean VISIBLE = new ImBoolean(true);

	public static void render(FrameInfo frame, ClockValue value, ClockLocation location) {
		var mc = frame.mc();
		float delta = frame.worldDelta();
		var font = location.font().get();

		if (font == null) {
			return;
		}

		var ms = frame.poseStack();

		var text = location.format().formatted(value.second() / 60, value.second() % 60).toCharArray();
		var width = font.getWidth(text);

		if (width <= 0) {
			return;
		}

		var light = location.fullbright() ? LightTexture.FULL_BRIGHT : LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, location.pos()), mc.level.getBrightness(LightLayer.SKY, location.pos()));

		ms.pushPose();
		frame.translate(location.pos().getX() + 0.5D, location.pos().getY() + location.offset() + 0.5D, location.pos().getZ() + 0.5D);
		ms.mulPose(Axis.YP.rotationDegrees(-location.facing().toYRot()));

		ms.scale(location.scale(), -location.scale(), -1F);
		var m4 = ms.last().pose();

		var buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(font.texture()));
		var x = -width / 2F + 1F;
		var y = -font.size().y() / 2F;
		var z = 0.4F;

		var color = location.color().lerp(switch (value.type()) {
			case FINISHED -> 1F;
			case FLASH -> 0.65F + Mth.cos((mc.player.vl$sessionData().tick - 1F + delta) * 0.85F) * 0.35F;
			default -> 0F;
		}, Clock.RED);

		int cr = color.red();
		int cg = color.green();
		int cb = color.blue();
		int ca = 255;

		for (var c : text) {
			int index = c >= '0' && c <= '9' ? (c - '0') : 10;
			var uv = font.uvs().get(index);
			float u0 = uv.u0();
			float v0 = uv.v0();
			float u1 = uv.u1();
			float v1 = uv.v1();
			float cw = index == 10 ? font.actualSeparatorWidth() : font.size().x();
			float ch = font.size().y();

			buffer.addVertex(m4, x, y, z).setColor(cr, cg, cb, ca).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
			buffer.addVertex(m4, x + cw, y, z).setColor(cr, cg, cb, ca).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
			buffer.addVertex(m4, x + cw, y + ch, z).setColor(cr, cg, cb, ca).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
			buffer.addVertex(m4, x, y + ch, z).setColor(cr, cg, cb, ca).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);

			x += cw + 1F;
		}

		ms.popPose();
	}
}
