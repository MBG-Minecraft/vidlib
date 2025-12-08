package dev.latvian.mods.vidlib.feature.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MiscClientUtils {
	public static final ConcurrentLinkedDeque<AutoCloseable> CLIENT_CLOSEABLE = new ConcurrentLinkedDeque<>();
	public static final Matrix4f FRUSTUM_MATRIX = new Matrix4f();
	public static final Matrix4f PERSPECTIVE_MATRIX = new Matrix4f();

	public static void reloadShaders(Minecraft mc) {
		mc.getShaderManager().reload(CompletableFuture::completedFuture, mc.getResourceManager(), Util.backgroundExecutor(), mc).thenRunAsync(() -> {
			mc.levelRenderer.onResourceManagerReload(mc.getResourceManager());
			// CompiledShader.Type.FRAGMENT.getPrograms().clear();
			// CompiledShader.Type.VERTEX.getPrograms().clear();
			mc.player.displayClientMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN), true);
		}, mc);
	}

	public static void renderVisuals(PoseStack ms, Vec3 cameraPos, MultiBufferSource buffers, BufferSupplier type, Visuals visuals, float progress) {
		for (var cube : visuals.cubes()) {
			ms.pushPose();
			float x = (float) (cube.pos().x - cameraPos.x);
			float y = (float) (cube.pos().y - cameraPos.y);
			float z = (float) (cube.pos().z - cameraPos.z);
			ms.translate(x, y, z);
			ms.mulPose(Axis.YP.rotation(cube.rotation().yawRad()));
			ms.mulPose(Axis.XP.rotation(cube.rotation().pitchRad()));
			ms.mulPose(Axis.ZP.rotation(cube.rotation().rollRad()));
			CuboidRenderer.voxelShapeBox(ms, cube.shape(), Vec3.ZERO, buffers, type, true, cube.color().isTransparent() ? Color.TRANSPARENT : cube.color().withAlpha(100), cube.lineColor());
			ms.popPose();
		}

		if (!visuals.shapes().isEmpty()) {
			var quadsBuffer = ms.last().transform(type.quads(buffers, true));

			for (var shape : visuals.shapes()) {
				var col = shape.shape().quads().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					shape.shape().shape().buildQuads(rx, ry, rz, quadsBuffer.withColor(col.withAlpha(100)));
				}
			}
		}

		/*
		if (!visuals.brightShapes().isEmpty()) {
			var quadsBuffer = ms.last().transform(type.quads(buffers, true));

			for (var shape : visuals.shapes()) {
				var col = shape.shape().quads().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					shape.shape().shape().buildQuads(rx, ry, rz, quadsBuffer.withColor(col.withAlpha(50)));
				}
			}
		}
		 */

		if (!visuals.outlineShapes().isEmpty()) {
			//POSITION_TEX_COLOR
			var quadsBuffer = ms.last().transform(buffers.getBuffer(VidLibRenderTypes.OUTLINE.apply(Empty.TEXTURE))).onlyPosColTex().withTex(UV.FULL);

			for (var shape : visuals.outlineShapes()) {
				var col = shape.shape().quads().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					shape.shape().shape().buildQuads(rx, ry, rz, quadsBuffer.withColor(col.withAlpha(100)));
				}
			}
		}

		if (!visuals.lines().isEmpty() || !visuals.shapes().isEmpty()) {
			var linesBuffer = ms.last().transform(type.lines(buffers));

			for (var line : visuals.lines()) {
				float rx = (float) (line.line().start().x - cameraPos.x);
				float ry = (float) (line.line().start().y - cameraPos.y);
				float rz = (float) (line.line().start().z - cameraPos.z);

				float dx = (float) line.line().dx();
				float dy = (float) line.line().dy();
				float dz = (float) line.line().dz();

				linesBuffer.acceptPos(rx, ry, rz).acceptCol(line.startColor().redf(), line.startColor().greenf(), line.startColor().bluef(), line.startColor().alphaf()).acceptNormal(dx, dy, dz);
				linesBuffer.acceptPos(rx + dx, ry + dy, rz + dz).acceptCol(line.endColor().redf(), line.endColor().greenf(), line.endColor().bluef(), line.endColor().alphaf()).acceptNormal(dx, dy, dz);
			}

			for (var shape : visuals.shapes()) {
				var col = shape.shape().lines().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					shape.shape().shape().buildLines(rx, ry, rz, linesBuffer.withColor(col));
				}
			}
		}
	}
}
