package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.icon.AtlasSpriteIcon;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.ItemIcon;
import dev.latvian.mods.vidlib.feature.icon.SimpleColorIcon;
import dev.latvian.mods.vidlib.feature.icon.TextureIcon;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IconRenderer {
	static <T extends Icon> void register(SimpleRegistryType<T> type, IconGuiRenderer<T> factory) {
		IconGuiRenderer.MAP.put(type, factory);
	}

	static <T extends Icon> void register(SimpleRegistryType<T> type, IconWorldRenderer<T> factory) {
		IconWorldRenderer.MAP.put(type, factory);
	}

	@AutoInit(AutoInit.Type.CLIENT_LOADED)
	static void bootstrap() {
		register(SimpleColorIcon.TYPE, ColorIconRenderer::draw);
		register(TextureIcon.TYPE, TextureIconRenderer::draw);
		register(ItemIcon.TYPE, ItemIconRenderer::draw);
		register(AtlasSpriteIcon.TYPE, AtlasSpriteIconRenderer::draw);

		register(SimpleColorIcon.TYPE, ColorIconRenderer::render);
		register(TextureIcon.TYPE, TextureIconRenderer::render);
		register(ItemIcon.TYPE, ItemIconRenderer::render);
		register(AtlasSpriteIcon.TYPE, AtlasSpriteIconRenderer::render);
	}

	static void draw(Icon icon, Minecraft mc, GuiGraphics graphics, int alpha) {
		var r = IconGuiRenderer.MAP.get(icon.type());

		if (r != null) {
			r.draw(Cast.to(icon), mc, graphics, alpha);
		}
	}

	static void render(Icon icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var r = IconWorldRenderer.MAP.get(icon.type());

		if (r != null) {
			r.render(Cast.to(icon), mc, ms, delta, source, light, overlay);
		}
	}
}
