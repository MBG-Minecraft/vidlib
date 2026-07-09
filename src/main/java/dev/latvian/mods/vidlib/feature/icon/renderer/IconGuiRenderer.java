package dev.latvian.mods.vidlib.feature.icon.renderer;

import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Map;

public interface IconGuiRenderer<T extends Icon> {
	Map<CustomRegistryType<?>, IconGuiRenderer<?>> MAP = new Reference2ObjectArrayMap<>(5);

	void draw(T icon, Minecraft mc, GuiGraphicsExtractor graphics, int alpha);
}
