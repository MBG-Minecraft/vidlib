package dev.beast.mods.shimmer.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public enum EmptyIconRenderer implements IconRenderer {
	INSTANCE;

	@Override
	public void render3D(Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
	}
}
