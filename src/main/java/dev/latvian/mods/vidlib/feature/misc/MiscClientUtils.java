package dev.latvian.mods.vidlib.feature.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.kmath.render.CuboidRenderer;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MiscClientUtils {
	public static final ConcurrentLinkedDeque<AutoCloseable> CLIENT_CLOSEABLE = new ConcurrentLinkedDeque<>();

	public static boolean handleDebugKeys(Minecraft mc, int key) {
		if (key == VidLibConfig.cycleShadersKey) {
			if (Screen.hasShiftDown()) {
				mc.execute(mc.gameRenderer::clearPostEffect);
			} else {
				// minecraft.submit(minecraft.gameRenderer::cycleSuperSecretSetting);
			}

			return false;
		} else if (key == VidLibConfig.reloadShadersKey) {
			reloadShaders(mc);
			return true;
		}

		return false;
	}

	public static void reloadShaders(Minecraft mc) {
		mc.getShaderManager().reload(CompletableFuture::completedFuture, mc.getResourceManager(), Util.backgroundExecutor(), mc).thenRunAsync(() -> {
			mc.levelRenderer.onResourceManagerReload(mc.getResourceManager());
			// CompiledShader.Type.FRAGMENT.getPrograms().clear();
			// CompiledShader.Type.VERTEX.getPrograms().clear();
			mc.player.displayClientMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN), true);
		}, mc);
	}

	public static boolean shouldShowName(Entity entity) {
		// var mc = Minecraft.getInstance();
		// return entity instanceof LocalPlayer && mc.isLocalServer() && !mc.options.getCameraType().isFirstPerson() || entity.hasCustomName();
		return !entity.isInvisible() && (entity instanceof LocalPlayer || entity.hasCustomName());
	}

	public static float depthFar(float renderDistance) {
		return 8192F;
	}

	public static int calculateScale(int w, int h) {
		if (VidLibConfig.forceHalfAuto) {
			int i = 1;

			while (i < w && i < h && w / (i + 1) >= 320 && h / (i + 1) >= 240) {
				i++;
			}

			if (i <= 3) {
				return 2;
			} else {
				return (i + 1) / 2;
			}
		}

		return -1;
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
			CuboidRenderer.voxelShapeBox(ms, cube.shape(), Vec3.ZERO, buffers, type, false, cube.color().withAlpha(50), cube.lineColor());
			ms.popPose();
		}

		if (!visuals.shapes().isEmpty()) {
			var quadsBuffer = ms.last().transform(type.quads(buffers, false));

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

		if (!visuals.lines().isEmpty() || !visuals.shapes().isEmpty()) {
			var linesBuffer = ms.last().transform(type.lines(buffers));

			for (var line : visuals.lines()) {
				float rx = (float) (line.line().start().x - cameraPos.x);
				float ry = (float) (line.line().start().y - cameraPos.y);
				float rz = (float) (line.line().start().z - cameraPos.z);

				linesBuffer.acceptPos(rx, ry, rz).acceptCol(line.startColor().redf(), line.startColor().greenf(), line.startColor().bluef(), line.startColor().alphaf());
				linesBuffer.acceptPos(rx + (float) line.line().dx(), ry + (float) line.line().dy(), rz + (float) line.line().dz()).acceptCol(line.endColor().redf(), line.endColor().greenf(), line.endColor().bluef(), line.endColor().alphaf());
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
