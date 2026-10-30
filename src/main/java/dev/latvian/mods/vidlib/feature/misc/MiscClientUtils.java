package dev.latvian.mods.vidlib.feature.misc;

import com.google.common.hash.Hashing;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MiscClientUtils {
	public static final ConcurrentLinkedDeque<AutoCloseable> CLIENT_CLOSEABLE = new ConcurrentLinkedDeque<>();
	public static final ImBoolean PLAYER_HEADWEAR = new ImBoolean(true);
	public static final ImBoolean SPECTATE_UI = new ImBoolean(false);
	public static final Map<Identifier, NativeImage> BUILTIN_SKIN_IMAGE_MAP = new HashMap<>();
	public static final Map<Identifier, NativeImage> SKIN_IMAGE_MAP = new HashMap<>();
	public static final Map<UUID, PlayerModelType> UUID_MODEL_MAP = new HashMap<>();
	public static final Map<String, PlayerModelType> SKIN_MODEL_MAP = new HashMap<>();

	public static final Set<Item> NO_BOB_ITEMS = new ReferenceArraySet<>(Set.of(
		Items.COMPASS,
		Items.RECOVERY_COMPASS
	));

	public static void reloadShaders(Minecraft mc) {
		mc.reloadResourcePacks().thenRunAsync(() -> {
			mc.player.sendSystemMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN));
		}, mc);
	}

	public static void renderVisuals(PoseStack ms, Vec3 cameraPos, MultiBufferSource buffers, BufferSupplier type, Visuals visuals, float progress) {
		for (var cube : visuals.cubes()) {
			ms.pushPose();
			float x = (float) (cube.pos().x - cameraPos.x);
			float y = (float) (cube.pos().y - cameraPos.y);
			float z = (float) (cube.pos().z - cameraPos.z);
			ms.translate(x, y, z);
			ms.rotate(Axis.YP, cube.rotation().yawRad());
			ms.rotate(Axis.XP, cube.rotation().pitchRad());
			ms.rotate(Axis.ZP, cube.rotation().rollRad());
			CuboidRenderer.voxelShapeBox(ms, cube.shape(), Vec3.ZERO, buffers, type, true, cube.color().isTransparent() ? Color.TRANSPARENT : cube.color().withAlpha(100), cube.lineColor());
			ms.popPose();
		}

		if (!visuals.shapes().isEmpty()) {
			var quadsBuffer = PoseTransforms.transform(ms.last(), type.quads(buffers, true));

			for (var shape : visuals.shapes()) {
				var col = shape.shape().quads().value().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					Shapes.buildQuads(shape.shape().shape().value(), rx, ry, rz, quadsBuffer.withColor(col.withAlpha(100)));
				}
			}
		}

		/*
		if (!visuals.brightShapes().isEmpty()) {
			var quadsBuffer = PoseTransforms.transform(ms.last(), type.quads(buffers, true));

			for (var shape : visuals.shapes()) {
				var col = shape.shape().quads().value().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					Shapes.buildQuads(shape.shape().shape().value(), rx, ry, rz, quadsBuffer.withColor(col.withAlpha(50)));
				}
			}
		}
		 */

		if (!visuals.outlineShapes().isEmpty()) {
			//POSITION_TEX_COLOR
			var quadsBuffer = PoseTransforms.transform(ms.last(), buffers.getBuffer(VidLibRenderTypes.OUTLINE.apply(Empty.TEXTURE))).onlyPosColTex().withTex(UV.FULL);

			for (var shape : visuals.outlineShapes()) {
				var col = shape.shape().quads().value().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					Shapes.buildQuads(shape.shape().shape().value(), rx, ry, rz, quadsBuffer.withColor(col.withAlpha(100)));
				}
			}
		}

		if (!visuals.lines().isEmpty() || !visuals.shapes().isEmpty()) {
			var linesBuffer = PoseTransforms.transform(ms.last(), type.lines(buffers));

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
				var col = shape.shape().lines().value().get(progress);

				if (!col.isTransparent()) {
					float rx = (float) (shape.position().x - cameraPos.x);
					float ry = (float) (shape.position().y - cameraPos.y);
					float rz = (float) (shape.position().z - cameraPos.z);
					Shapes.buildLines(shape.shape().shape().value(), rx, ry, rz, linesBuffer.withColor(col));
				}
			}
		}
	}

	public static int drawStackSize(GuiGraphicsExtractor graphics, Font font, String size, int x, int y, int color, boolean dropShadow) {
		var ms = graphics.pose();
		int w = font.width(size);
		float scale = size.length() >= 4 ? 0.5F : size.length() == 3 ? 0.75F : 1F;
		ms.pushMatrix();
		ms.translate((int) (x + 16F - (w - 1F) * scale), (int) (y + 16F - 7F * scale));
		ms.scale(scale, scale);
		int s = font.width(size);
		graphics.text(font, size, 0, 0, color, dropShadow);
		ms.popMatrix();
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

	public static void setModel(UUID uuid, MinecraftProfileTextures textures, PlayerModelType model) {
		if (textures.skin() != null) {
			var hash = Hashing.sha1().hashUnencodedChars(textures.skin().getHash()).toString();
			UUID_MODEL_MAP.put(uuid, model);
			SKIN_MODEL_MAP.put(hash, model);
		}
	}

	public static void delayedExecute(Minecraft mc, Runnable task) {
		Thread.startVirtualThread(() -> mc.execute(task));
	}
}
