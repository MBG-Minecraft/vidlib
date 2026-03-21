package dev.latvian.mods.vidlib.feature.font;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import imgui.type.ImFloat;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
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

/**
 * <ol>
 *   <li>Download latest <a href="https://github.com/Chlumsky/msdf-atlas-gen/releases">MSDF Atlas Gen</a></li>
 *   <li>Create textures/, json/, ttf/ directories in the same directory as msdf-atlas-gen.exe</li>
 *   <li>Place your font in ttf/</li>
 *   <li>Open Terminal in directory and run <code>.\msdf-atlas-gen.exe -outerpxpadding 1 -type mtsdf -imageout "textures/example.png" -json "json/example.json" -font "ttf/example.ttf"</code></li>
 * </ol>
 */
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

	public static final MSDFFont UNKNOWN = new MSDFFont(
		createKey(VidLib.id("unknown")),
		VidLibTextures.MISSING,
		MSDFFontData.EMPTY,
		Char2ObjectMaps.emptyMap(),
		"",
		VidLibRenderTypes.MSDF.apply(VidLibTextures.MISSING),
		VidLibRenderTypes.MSDF_SEE_THROUGH.apply(VidLibTextures.MISSING)
	);

	public record GlyphInfo(
		char unicode,
		float advance,
		@Nullable MSDFFontData.Bounds plane,
		@Nullable MSDFFontData.Bounds uv
	) {
	}

	public static Map<ResourceKey<MSDFFont>, MSDFFont> ALL = Map.of();

	public static MSDFFont getFont(ResourceKey<MSDFFont> key) {
		return ALL.getOrDefault(key, UNKNOWN);
	}

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
		graphics.pose().translate(20F, 0F, 0F);
		graphics.pose().scale(size, size, 1F);

		var mbgFont = getFont(MSDFFonts.KOMIKA_AXIS);

		mbgFont.drawWithShadow(graphics.pose(), graphics.vl$buffers(), "MrBeast Gaming", 0xFF35D9F3);
		graphics.pose().translate(0F, mbgFont.data.metrics().lineHeight(), 0F);

		for (var font : ALL.values()) {
			font.drawWithShadow(graphics.pose(), graphics.vl$buffers(), font.visibleGlyphs, 0xFFFFFFFF);
			graphics.pose().translate(0F, font.data.metrics().lineHeight(), 0F);
		}

		graphics.pose().popPose();
		graphics.flush();
	}

	public float draw(PoseStack ms, MultiBufferSource buffers, String text, int color) {
		return draw(ms, buffers, text, color, color, color, color);
	}

	public float draw(PoseStack ms, MultiBufferSource buffers, String text, int tlArgb, int trArgb, int blArgb, int brArgb) {
		return build(ms.last().pose(), buffers.getBuffer(renderType), text, tlArgb, trArgb, blArgb, brArgb);
	}

	public float drawWithShadow(PoseStack ms, MultiBufferSource buffers, String text, int color) {
		return drawWithShadow(ms, buffers, text, color, color, color, color);
	}

	public float drawWithShadow(PoseStack ms, MultiBufferSource buffers, String text, int tlArgb, int trArgb, int blArgb, int brArgb) {
		return buildWithShadow(ms.last().pose(), buffers.getBuffer(renderType), text, 0.075F, 0F, tlArgb, trArgb, blArgb, brArgb);
	}

	public float buildWithShadow(Matrix4f m, VertexConsumer buffer, String text, float offset, float depth, int tlArgb, int trArgb, int blArgb, int brArgb) {
		m.translate(offset, offset, depth);
		build(m, buffer, text, ARGB.scaleRGB(tlArgb, 0.25F), ARGB.scaleRGB(trArgb, 0.25F), ARGB.scaleRGB(blArgb, 0.25F), ARGB.scaleRGB(brArgb, 0.25F));
		m.translate(-offset, -offset, depth);
		return build(m, buffer, text, tlArgb, trArgb, blArgb, brArgb) + offset;
	}

	public float build(Matrix4f m, VertexConsumer buffer, String text, int tlArgb, int trArgb, int blArgb, int brArgb) {
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
