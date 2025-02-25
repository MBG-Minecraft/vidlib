package dev.beast.mods.shimmer.content.clock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ClockBlockEntityRenderer implements BlockEntityRenderer<ClockBlockEntity> {
	public ClockBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {
	}

	@Override
	public void render(ClockBlockEntity entity, float delta, PoseStack ms, MultiBufferSource source, int light, int overlay) {
		var type = entity.getBlockState().getValue(ClockBlock.TYPE);
		var time = Mth.lerp(delta, ClockBlockEntity.prevTicks, ClockBlockEntity.ticks);
		var itime = (int) time;
		var text = entity.format.formatted(itime / 1200, (itime / 20) % 60).toCharArray();
		var width = type.getWidth(text);

		if (width <= 0) {
			return;
		}

		var noOverlay = OverlayTexture.NO_OVERLAY;
		var fullBright = LightTexture.FULL_BRIGHT;

		ms.pushPose();
		ms.translate(0.5, 0.5, 0.5);
		ms.mulPose(Axis.YP.rotationDegrees(-entity.getBlockState().getValue(ClockBlock.FACING).toYRot()));

		ms.scale(1f, -1f, -1f);
		var m4 = ms.last().pose();
		var normal = new Vector3f(0F, -1F, 0F).mul(ms.last().normal());

		var buffer = source.getBuffer(RenderType.entityCutoutNoCull(ClockType.TEXTURE));
		var x = -width / 2f + 1f;
		var y = -type.height / 2f;
		var z = 0.4F;

		float col = time > 200F ? 1F : time <= 19F ? 0.3F : (0.65F + Mth.sin(time * 0.85F) * 0.35F);

		for (var c : text) {
			int index = c >= '0' && c <= '9' ? (c - '0') : 10;
			float u0 = type.uvs[index][0];
			float v0 = type.uvs[index][1];
			float u1 = type.uvs[index][2];
			float v1 = type.uvs[index][3];
			float cw = index == 10 ? type.colonWidth : type.width;

			buffer.addVertex(m4, x, y, z).setColor(1f, col, col, 1f).setUv(u0, v0).setOverlay(noOverlay).setLight(fullBright).setNormal(normal.x, normal.y, normal.z);
			buffer.addVertex(m4, x + cw, y, z).setColor(1f, col, col, 1f).setUv(u1, v0).setOverlay(noOverlay).setLight(fullBright).setNormal(normal.x, normal.y, normal.z);
			buffer.addVertex(m4, x + cw, y + type.height, z).setColor(1f, col, col, 1f).setUv(u1, v1).setOverlay(noOverlay).setLight(fullBright).setNormal(normal.x, normal.y, normal.z);
			buffer.addVertex(m4, x, y + type.height, z).setColor(1f, col, col, 1f).setUv(u0, v1).setOverlay(noOverlay).setLight(fullBright).setNormal(normal.x, normal.y, normal.z);

			x += (c == ':' ? type.colonWidth : type.width) + 1F;
		}

		ms.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(ClockBlockEntity entity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 1000;
	}

	@Override
	public boolean shouldRender(ClockBlockEntity blockEntity, Vec3 cameraPos) {
		return true;
	}

	@Override
	public AABB getRenderBoundingBox(ClockBlockEntity entity) {
		return AABB.INFINITE;
	}
}
