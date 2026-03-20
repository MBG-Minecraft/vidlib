package dev.latvian.mods.vidlib.feature.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import imgui.type.ImFloat;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.IdentityHashMap;
import java.util.Map;

public record MSDFFont(
	ResourceKey<MSDFFont> key,
	ResourceLocation texture,
	MSDFFontData data,
	Char2ObjectMap<GlyphInfo> glyphs,
	String visibleGlyphs,
	RenderType renderType,
	RenderType seeThroughRenderType
) {
	public static final ImFloat DEBUG_SIZE = new ImFloat(0F);

	private static final ResourceKey<? extends Registry<MSDFFont>> ROOT_ID = ResourceKey.createRegistryKey(VidLib.id("msdf_font"));

	public static ResourceKey<MSDFFont> createKey(ResourceLocation id) {
		return ResourceKey.create(ROOT_ID, id);
	}

	public record GlyphInfo(
		char unicode,
		float advance,
		@Nullable MSDFFontData.Bounds plane,
		@Nullable MSDFFontData.Bounds uv
	) {
	}

	public static Map<ResourceKey<MSDFFont>, MSDFFont> ALL = Map.of();

	public static class Loader extends JsonCodecReloadListener<MSDFFontData> {
		public Loader() {
			super("vidlib/msdf", MSDFFontData.CODEC, false);
		}

		@Override
		protected void apply(ResourceManager resourceManager, Map<ResourceLocation, MSDFFontData> map) {
			var fontMap = new IdentityHashMap<ResourceKey<MSDFFont>, MSDFFont>();

			for (var entry : map.entrySet()) {
				var data = entry.getValue();
				var texture = entry.getKey().withPath(p -> "textures/font/msdf/" + p + ".png");
				var glyphs = new Char2ObjectOpenHashMap<GlyphInfo>();

				var visibleGlyphs = new CharArrayList();

				for (var glyph : data.glyphs()) {
					if (glyph.unicode() > ' ') {
						visibleGlyphs.add(glyph.unicode());
					}

					MSDFFontData.Bounds uv = null;

					if (glyph.atlasBounds().isPresent()) {
						var atlas = glyph.atlasBounds().get();

						uv = new MSDFFontData.Bounds(
							atlas.left() / data.atlas().width(),
							(data.atlas().height() - atlas.top()) / data.atlas().height(),
							atlas.right() / data.atlas().width(),
							(data.atlas().height() - atlas.bottom()) / data.atlas().height()
						);
					}

					var plane = glyph.planeBounds().orElse(null);

					glyphs.put(glyph.unicode(), new GlyphInfo(
						glyph.unicode(),
						glyph.advance(),
						plane == null ? MSDFFontData.Bounds.EMPTY : plane,
						uv
					));
				}

				visibleGlyphs.sort(null);

				var key = createKey(entry.getKey());

				fontMap.put(key, new MSDFFont(
					key,
					texture,
					data,
					glyphs,
					new String(visibleGlyphs.toCharArray()),
					VidLibRenderTypes.MSDF.apply(texture),
					VidLibRenderTypes.MSDF_SEE_THROUGH.apply(texture)
				));
			}

			ALL = Map.copyOf(fontMap);
		}
	}

	public static void drawDebugText(GuiGraphics graphics, DeltaTracker deltaTracker) {
		float size = DEBUG_SIZE.get();

		if (size <= 0F) {
			return;
		}

		graphics.flush();
		graphics.pose().pushPose();
		graphics.pose().translate(20F, 20F, 0F);
		graphics.pose().scale(size, size, 1F);

		for (var font : ALL.values()) {
			var buffer = graphics.vl$buffers().getBuffer(font.renderType);
			font.drawWithShadow(graphics.pose().last().pose(), buffer, font.visibleGlyphs, 0xFFFFFFFF);
			graphics.pose().translate(0F, font.data.metrics().lineHeight(), 0F);
		}

		graphics.pose().popPose();
		graphics.flush();
	}

	public float draw(Matrix4f m, VertexConsumer buffer, String text, int color) {
		return draw(m, buffer, text, color, color, color, color);
	}

	public float drawWithShadow(Matrix4f m, VertexConsumer buffer, String text, int color) {
		m.translate(0.075F, 0.075F, 0F);
		draw(m, buffer, text, ARGB.scaleRGB(color, 0.25F));
		m.translate(-0.075F, -0.075F, 0F);
		return draw(m, buffer, text, color);
	}

	public float draw(Matrix4f m, VertexConsumer buffer, String text, int tlArgb, int trArgb, int blArgb, int brArgb) {
		float x = 0F;
		float y = data.metrics().lineHeight() + data.metrics().descender();
		int len = text.length();

		for (int i = 0; i < len; i++) {
			var g = glyphs.get(text.charAt(i));

			if (g != null) {
				var uv = g.uv;
				var p = g.plane;

				if (p != null && uv != null) {
					buffer.addVertex(m, x + p.left(), y - p.bottom(), 0F).setUv(uv.left(), uv.top()).setColor(trArgb);
					buffer.addVertex(m, x + p.right(), y - p.bottom(), 0F).setUv(uv.right(), uv.top()).setColor(tlArgb);
					buffer.addVertex(m, x + p.right(), y - p.top(), 0F).setUv(uv.right(), uv.bottom()).setColor(blArgb);
					buffer.addVertex(m, x + p.left(), y - p.top(), 0F).setUv(uv.left(), uv.bottom()).setColor(brArgb);
				}

				x += g.advance;
			}
		}

		return x;
	}
}
