package dev.latvian.mods.vidlib.feature.icon.renderer;

import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;

public interface IconGuiRenderer<T extends Icon> {
	Map<SimpleRegistryType<?>, IconGuiRenderer<?>> MAP = new Reference2ObjectArrayMap<>(5);

	void draw(T icon, Minecraft mc, GuiGraphics graphics, int alpha);
}
