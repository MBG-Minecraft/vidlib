package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gl.GLDebugLog;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.util.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiStatus.Internal
public class CanvasImpl {
	static final List<Canvas> ENABLED = new ArrayList<>();
	static final List<ExternalCanvas> ENABLED_EXT = new ArrayList<>();

	public static void initAll(Minecraft mc, ResourceManager manager) {
		for (var canvas : ENABLED_EXT) {
			canvas.close();
		}

		ENABLED.clear();
		ENABLED_EXT.clear();

		var all = Canvas.ALL.get();

		for (var canvas : all.values()) {
			canvas.enabled = false;
			canvas.data = CanvasData.DEFAULT;
		}

		for (var entry : manager.listResources("vidlib/canvas", p -> p.getPath().endsWith(".json")).entrySet()) {
			var id = entry.getKey().withPath(p -> p.substring(14, p.length() - 5));

			try (var in = entry.getValue().open()) {
				var json = JsonUtils.read(in).getAsJsonObject();
				var canvas = all.get(id);

				if (canvas != null) {
					canvas.enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
					canvas.data = CanvasData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
				} else {
					VidLib.LOGGER.error("Uninitialized canvas " + id);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		int w = mc.getWindow().getWidth();
		int h = mc.getWindow().getHeight();

		for (var canvas : all.values()) {
			if (canvas.enabled) {
				if (canvas instanceof ExternalCanvas ext) {
					ext.init(w, h);
				}

				mc.getTextureManager().register(canvas.colorTexturePath, new CanvasTexture(canvas, false));
				mc.getTextureManager().register(canvas.depthTexturePath, new CanvasTexture(canvas, true));

				ENABLED.add(canvas);
			}
		}

		ENABLED.sort((a, b) -> Integer.compare(b.data.priority(), a.data.priority()));

		for (var canvas : ENABLED) {
			if (canvas instanceof ExternalCanvas ext) {
				ENABLED_EXT.add(ext);
			}
		}

		VidLib.LOGGER.info("Canvas loaded (" + ENABLED.size() + "/" + all.size() + "): " + ENABLED);
	}

	public static void resizeAll(int width, int height) {
		GLDebugLog.pushGroup("Canvas Resize");

		for (var canvas : ENABLED_EXT) {
			GLDebugLog.message(canvas.idString);
			canvas.resize(width, height);
		}

		GLDebugLog.popGroup();
	}

	public static void drawAllBeforeOutline(Minecraft mc) {
		var texture = Objects.requireNonNull(mc.getMainRenderTarget().getColorTexture());
		GLDebugLog.pushGroup("Canvas Draw All Before Outline");

		for (var canvas : ENABLED) {
			if (canvas.data.priority() >= 0) {
				if (canvas.data.autoDraw()) {
					GLDebugLog.message(canvas.idString);
					canvas.draw(mc, texture);
				} else if (canvas.drawCallback != null) {
					canvas.drawCallback.accept(mc);
				}
			}
		}

		GLDebugLog.popGroup();
	}

	public static void drawAllAfterOutline(Minecraft mc) {
		var texture = Objects.requireNonNull(mc.getMainRenderTarget().getColorTexture());
		GLDebugLog.pushGroup("Canvas Draw All After Outline");

		for (var canvas : ENABLED) {
			if (canvas.data.priority() < 0) {
				if (canvas.data.autoDraw()) {
					GLDebugLog.message(canvas.idString);
					canvas.draw(mc, texture);
				} else if (canvas.drawCallback != null) {
					canvas.drawCallback.accept(mc);
				}
			}
		}

		GLDebugLog.popGroup();
	}

	public static void closeAll() {
		for (var canvas : ENABLED_EXT) {
			canvas.close();
		}
	}

	@Nullable
	public static Canvas get(ResourceLocation id) {
		var c = Canvas.ALL.get().get(id);
		return c != null && c.enabled ? c : null;
	}

	public static boolean replace(ResourceLocation id, ResourceHandle<RenderTarget> target) {
		var canvas = get(id);

		if (canvas != null) {
			canvas.outputTarget = target;
			return true;
		}

		return false;
	}

	public static void clearAll() {
		for (var canvas : ENABLED) {
			canvas.outputTarget = null;
		}
	}

	public static void createHandles(FrameGraphBuilder builder, RenderTargetDescriptor targetDescriptor) {
		GLDebugLog.pushGroup("Canvas Create Handles");

		for (var canvas : ENABLED) {
			GLDebugLog.message(canvas.idString);
			canvas.createHandle(builder, targetDescriptor);

			if (canvas.data.autoClear()) {
				canvas.clear();
			}
		}

		GLDebugLog.popGroup();
	}

	public static void addAllToFrame(Minecraft mc, FrameGraphBuilder frameGraphBuilder, PostChain.TargetBundle targetBundle) {
		int w = mc.getMainRenderTarget().width;
		int h = mc.getMainRenderTarget().height;
		GLDebugLog.pushGroup("Canvas Add to Frame " + w + " x " + h);

		for (var canvas : ENABLED_EXT) {
			GLDebugLog.message(canvas.idString);
			canvas.addToFrame(mc, frameGraphBuilder, targetBundle, w, h);
		}

		GLDebugLog.popGroup();
	}

	public static void allReadsAndWrites(FramePass pass) {
		GLDebugLog.pushGroup("Canvas Reads and Writes");

		for (var canvas : ENABLED) {
			GLDebugLog.message(canvas.idString);
			canvas.readsAndWrites(pass);
		}

		GLDebugLog.popGroup();
	}

	public static void drawPreview(Minecraft mc, GuiGraphics g) {
		if (mc.level == null || mc.vl$hideGui() || mc.level.isReplayLevel()) {
			return;
		}

		GLDebugLog.pushGroup("Canvas Preview");

		int y = 5;
		var buffers = mc.renderBuffers().bufferSource();
		float scale = 0.5F;
		int w = (int) (160F * scale);
		int h = (int) (90F * scale);

		for (var canvas : ENABLED) {
			int x = 5;

			if (canvas.previewColor || canvas.previewDepth) {
				GLDebugLog.pushGroup(canvas.idString);

				g.pose().pushPose();
				g.pose().translate(0F, 0F, 950F);
				var p = g.pose().last();
				var m = p.pose();

				if (canvas.previewColor) {
					GLDebugLog.message("Color Preview");
					var tex = canvas.getColorTexture();

					g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
					g.fill(x, y, x + w, y + h, 0xFF232326);

					if (tex != null) {
						var buffer = buffers.getBuffer(VidLibRenderTypes.GUI.apply(canvas.colorTexturePath));
						buffer.addVertex(m, x, y, 0F).setUv(0F, 1F).setColor(255, 255, 255, 255);
						buffer.addVertex(m, x, y + h, 0F).setUv(0F, 0F).setColor(255, 255, 255, 255);
						buffer.addVertex(m, x + w, y + h, 0F).setUv(1F, 0F).setColor(255, 255, 255, 255);
						buffer.addVertex(m, x + w, y, 0F).setUv(1F, 1F).setColor(255, 255, 255, 255);

						var argb = Color.of(canvas.getCenterARGB());
						var text = argb.toARGBString();
						g.fill(x + 1, y + 1, x + 12 + mc.font.width(text), y + 10, 0xA0000000);
						g.fill(x + 2, y + 2, x + 9, y + 9, argb.argb() | 0xFF000000);
						g.drawString(mc.font, text, x + 12, y + 2, 0xFFFFFFFF);
					} else {
						var buffer = buffers.getBuffer(RenderType.debugLine(1D));
						buffer.addVertex(m, x, y, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);
						buffer.addVertex(m, x + w, y + h, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);

						buffer.addVertex(m, x, y + h, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);
						buffer.addVertex(m, x + w, y, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);
					}

					x += w + 6;
				}

				if (canvas.previewDepth) {
					GLDebugLog.message("Depth Preview");
					var tex = canvas.getDepthTexture();

					g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
					g.fill(x, y, x + w, y + h, 0xFF232326);

					if (tex != null) {
						var buffer = buffers.getBuffer(VidLibRenderTypes.GUI_DEPTH.apply(canvas.depthTexturePath));
						buffer.addVertex(m, x, y, 0F).setUv(0F, 1F).setColor(255, 255, 255, 255);
						buffer.addVertex(m, x, y + h, 0F).setUv(0F, 0F).setColor(255, 255, 255, 255);
						buffer.addVertex(m, x + w, y + h, 0F).setUv(1F, 0F).setColor(255, 255, 255, 255);
						buffer.addVertex(m, x + w, y, 0F).setUv(1F, 1F).setColor(255, 255, 255, 255);

						var depth = canvas.getCenterDepth();
						var text = String.format("%.05f", depth);
						g.fill(x + 1, y + 1, x + 2 + mc.font.width(text), y + 10, 0xA0000000);
						g.drawString(mc.font, text, x + 2, y + 2, 0xFFFFFFFF);

						var ltext = String.format("%.05f", mc.linearizeDepth(depth));
						g.fill(x + 1, y + h - 10, x + 2 + mc.font.width(ltext), y + h - 1, 0xA0000000);
						g.drawString(mc.font, ltext, x + 2, y + h - 9, 0xFFFFFFFF);
					} else {
						var buffer = buffers.getBuffer(RenderType.debugLine(1D));
						buffer.addVertex(m, x, y, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);
						buffer.addVertex(m, x + w, y + h, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);

						buffer.addVertex(m, x, y + h, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);
						buffer.addVertex(m, x + w, y, 0F).setColor(255, 0, 0, 255).setNormal(p, 0F, 1F, 0F);
					}
				}

				y += h + 6;
				g.pose().popPose();

				GLDebugLog.popGroup();
			}
		}

		GLDebugLog.popGroup();
	}
}
