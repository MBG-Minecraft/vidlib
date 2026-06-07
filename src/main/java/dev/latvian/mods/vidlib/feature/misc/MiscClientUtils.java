package dev.latvian.mods.vidlib.feature.misc;

import com.google.common.hash.Hashing;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MiscClientUtils {
	public static final ConcurrentLinkedDeque<AutoCloseable> CLIENT_CLOSEABLE = new ConcurrentLinkedDeque<>();
	public static final Matrix4f FRUSTUM_MATRIX = new Matrix4f();
	public static final Matrix4f PERSPECTIVE_MATRIX = new Matrix4f();
	public static final ImBoolean PLAYER_HEADWEAR = new ImBoolean(true);
	private static final char[] POWER = {'K', 'M', 'B', 'T'};
	public static final ImBoolean SPECTATE_UI = new ImBoolean(false);
	public static final Map<ResourceLocation, NativeImage> BUILTIN_SKIN_IMAGE_MAP = new HashMap<>();
	public static final Map<ResourceLocation, NativeImage> SKIN_IMAGE_MAP = new HashMap<>();
	public static final Map<UUID, PlayerSkin.Model> UUID_MODEL_MAP = new HashMap<>();
	public static final Map<String, PlayerSkin.Model> SKIN_MODEL_MAP = new HashMap<>();

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

	public static String formatNumber(int count) {
		if (Screen.hasAltDown()) {
			return String.format("%,d", count);
		}

		int index = 0;

		if (count > 9999) {
			while (count / 1000 != 0) {
				count /= 1000;
				index++;
			}
		}

		if (index > 0) {
			return count + String.valueOf(POWER[index - 1]);
		} else {
			return String.valueOf(count);
		}
	}

	public static int drawStackSize(GuiGraphics graphics, Font font, String size, int x, int y, int color, boolean dropShadow) {
		var ms = graphics.pose();
		int w = font.width(size);
		float scale = size.length() >= 4 ? 0.5F : size.length() == 3 ? 0.75F : 1F;
		ms.pushPose();
		ms.translate((int) (x + 16F - (w - 1F) * scale), (int) (y + 16F - 7F * scale), 200F);
		ms.scale(scale, scale, 1F);
		int s = graphics.drawString(font, size, 0F, 0F, color, dropShadow);
		ms.popPose();
		return Mth.ceil(s * scale);
	}

	public static float adjustScreenX(Minecraft mc, int adjustedWidth) {
		return (mc.getWindow().getGuiScaledWidth() - adjustedWidth) / 2F;
	}

	public static int adjustScreenWidth(Minecraft mc, boolean shifted) {
		if (shifted) {
			float w = mc.getWindow().getGuiScaledWidth();
			float h = mc.getWindow().getGuiScaledHeight();
			// figure out how to shift to 16:9 nicely
		}

		return mc.getWindow().getGuiScaledWidth();
	}

	public static void setModel(UUID uuid, MinecraftProfileTextures textures, PlayerSkin.Model model) {
		if (textures.skin() != null) {
			var hash = Hashing.sha1().hashUnencodedChars(textures.skin().getHash()).toString();
			UUID_MODEL_MAP.put(uuid, model);
			SKIN_MODEL_MAP.put(hash, model);
		}
	}

	public static NativeImage remapImage(NativeImage src, Int2IntMap remap) {
		return src.mappedCopy(srcArgb -> {
			if (srcArgb == 0) {
				return 0;
			}

			int dstArgb = remap.getOrDefault(srcArgb & 0xFFFFFF, srcArgb);

			if (dstArgb == 0x01010101) {
				return 0x01010101;
			}

			return (dstArgb & 0xFFFFFF) | (srcArgb & 0xFF000000);
		});
	}

	public static NativeImage layeredImage(List<NativeImage> layers, boolean keepMeta) {
		if (layers.isEmpty()) {
			throw new IllegalArgumentException("No layers");
		}

		int dstW = 0;
		int dstH = 0;

		for (var src : layers) {
			dstW = Math.max(dstW, src.getWidth());
			dstH = Math.max(dstH, src.getHeight());
		}

		if (dstW == 0 || dstH == 0) {
			throw new IllegalArgumentException("Invalid layer sizes");
		}

		var dst = new NativeImage(dstW, dstH, true);

		for (var src : layers) {
			int srcW = src.getWidth();
			int srcH = src.getHeight();

			for (int x = 0; x < dstW; x++) {
				for (int y = 0; y < dstH; y++) {
					int srcColorArgb = src.getPixel(x * srcW / dstW, y * srcH / dstH);

					if (srcColorArgb == 0x01010101) {
						dst.setPixel(x, y, keepMeta ? 0x01010101 : 0);
					} else {
						var srcColor = Color.of(srcColorArgb);
						var dstColor = Color.of(dst.getPixel(x, y));
						dst.setPixel(x, y, srcColor.mix(dstColor).argb());
					}
				}
			}
		}

		return dst;
	}
}
