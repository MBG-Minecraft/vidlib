package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Map;

public interface IconWorldRenderer<T extends Icon> {
	Map<CustomRegistryType<RegistryFriendlyByteBuf, Icon>, IconWorldRenderer<?>> MAP = new Reference2ObjectArrayMap<>(5);

	void render(T icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay);
}
