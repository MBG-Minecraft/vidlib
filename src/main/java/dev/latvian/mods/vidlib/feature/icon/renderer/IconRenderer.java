package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.icon.AtlasSpriteIcon;
import dev.latvian.mods.vidlib.feature.icon.ColorIcon;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.ItemIcon;
import dev.latvian.mods.vidlib.feature.icon.TextureIcon;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public interface IconRenderer {
	Map<SimpleRegistryType<?>, Function<? extends Icon, IconRenderer>> RENDERERS = new IdentityHashMap<>();

	static <T extends Icon> void register(SimpleRegistryType<?> type, Function<T, IconRenderer> factory) {
		RENDERERS.put(type, factory);
	}

	static IconRenderer create(Icon icon) {
		var r = RENDERERS.get(icon.type());
		var rr = r == null ? null : r.apply(Cast.to(icon));
		return rr == null ? EmptyIconRenderer.INSTANCE : rr;
	}

	@AutoInit(AutoInit.Type.CLIENT_LOADED)
	static void bootstrap() {
		register(ColorIcon.TYPE, ColorIconRenderer::new);
		register(TextureIcon.TYPE, TextureIconRenderer::new);
		register(ItemIcon.TYPE, ItemIconRenderer::new);
		register(AtlasSpriteIcon.TYPE, AtlasSpriteIconRenderer::new);
	}

	void render3D(Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay);
}
