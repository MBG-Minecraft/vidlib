package dev.latvian.mods.vidlib.feature.icon.renderer;

import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Map;

public interface IconGuiRenderer<T extends Icon> {
	Map<CustomRegistryType<RegistryFriendlyByteBuf, Icon>, IconGuiRenderer<?>> MAP = new Reference2ObjectArrayMap<>(5);

	void draw(T icon, Minecraft mc, GuiGraphicsExtractor graphics, int alpha);
}
