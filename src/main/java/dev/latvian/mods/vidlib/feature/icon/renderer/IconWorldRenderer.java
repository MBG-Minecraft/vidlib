package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Map;

public interface IconWorldRenderer<T extends Icon> {
	Map<SimpleRegistryType<?>, IconWorldRenderer<?>> MAP = new Reference2ObjectArrayMap<>(5);

	void render(T icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay);
}
