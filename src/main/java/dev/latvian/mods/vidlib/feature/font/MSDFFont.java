package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import imgui.type.ImFloat;
import it.unimi.dsi.fastutil.chars.Char2FloatMap;
import it.unimi.dsi.fastutil.chars.Char2FloatMaps;
import it.unimi.dsi.fastutil.chars.Char2FloatOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

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
	GlyphInfo spaceGlyph,
	String visibleGlyphs,
	RenderType renderType,
	RenderType seeThroughRenderType,
	int iteration
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
		new GlyphInfo(' ', 0F, null, null, Char2FloatMaps.EMPTY_MAP),
		"",
		VidLibRenderTypes.MSDF.apply(VidLibTextures.MISSING),
		VidLibRenderTypes.MSDF_SEE_THROUGH.apply(VidLibTextures.MISSING),
		0
	);

	public record GlyphInfo(
		char unicode,
		float advance,
		@Nullable MSDFFontData.Bounds plane,
		@Nullable MSDFFontData.Bounds uv,
		Char2FloatMap kerning
	) {
	}

	public static Map<ResourceKey<MSDFFont>, MSDFFont> ALL = Map.of();
	public static int registryIteration = 0;

	public static MSDFFont getFont(ResourceKey<MSDFFont> key) {
		return ALL.getOrDefault(key, UNKNOWN);
	}

	public static class Loader extends JsonCodecReloadListener<MSDFFontData> {
		public Loader() {
			super("vidlib/msdf", MSDFFontData.CODEC, false);
		}

		@Override
		protected void apply(ResourceManager resourceManager, Map<ResourceLocation, MSDFFontData> map) {
			registryIteration++;
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
						uv,
						new Char2FloatOpenHashMap()
					));
				}

				for (var kerning : data.kerning()) {
					var g = glyphs.get(kerning.unicode2());

					if (g != null) {
						g.kerning.put(kerning.unicode1(), kerning.advance());
					}
				}

				visibleGlyphs.sort(null);

				var key = createKey(entry.getKey());

				fontMap.put(key, new MSDFFont(
					key,
					texture,
					data,
					glyphs,
					glyphs.getOrDefault(' ', UNKNOWN.spaceGlyph),
					new String(visibleGlyphs.toCharArray()),
					VidLibRenderTypes.MSDF.apply(texture),
					VidLibRenderTypes.MSDF_SEE_THROUGH.apply(texture),
					registryIteration
				));
			}

			ALL = Map.copyOf(fontMap);
		}
	}

	public GlyphInfo getGlyph(char unicode) {
		return unicode == ' ' ? spaceGlyph : glyphs.getOrDefault(unicode, spaceGlyph);
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

		var fontRenderer = new MSDFRenderer();
		fontRenderer.setFont(MSDFFonts.KOMIKA_AXIS);
		fontRenderer.setCornerColors(0xFFFD418B, 0xFF35D9F3, 0xFF35D9F3, 0xFFFD418B);

		fontRenderer.draw(graphics, "MrBeast Gaming");
		fontRenderer.newLine();

		fontRenderer.setCornerColors(0xFFFF0000, 0xFFFFFF00, 0xFF0000FF, 0xFF00FF00);

		for (var font : ALL.values()) {
			fontRenderer.setFont(font);
			fontRenderer.draw(graphics, font.visibleGlyphs);
			fontRenderer.newLine();
		}

		graphics.pose().popPose();
		graphics.flush();
	}
}
