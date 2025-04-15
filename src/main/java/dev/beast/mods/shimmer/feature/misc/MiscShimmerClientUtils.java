package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public class MiscShimmerClientUtils {
	public static final ContextKey<Boolean> CREATIVE = new ContextKey<>(Shimmer.id("creative"));
	public static final ContextKey<Clothing> CLOTHING = new ContextKey<>(Shimmer.id("clothing"));

	public static KeyMapping freezeTickKeyMapping;
	public static KeyMapping clearParticlesKeyMapping;

	public static FogParameters fogOverride = FogParameters.NO_FOG;

	public static boolean handleDebugKeys(Minecraft mc, int key) {
		if (key == ShimmerConfig.cycleShadersKey) {
			if (Screen.hasShiftDown()) {
				mc.execute(mc.gameRenderer::clearPostEffect);
			} else {
				// minecraft.submit(minecraft.gameRenderer::cycleSuperSecretSetting);
			}

			return true;
		} else if (key == ShimmerConfig.reloadShadersKey) {
			reloadShaders(mc);
			return true;
		}

		return false;
	}

	public static void reloadShaders(Minecraft mc) {
		mc.getShaderManager().reload(CompletableFuture::completedFuture, mc.getResourceManager(), Util.backgroundExecutor(), mc).thenRunAsync(() -> {
			mc.levelRenderer.onResourceManagerReload(mc.getResourceManager());
			// CompiledShader.Type.FRAGMENT.getPrograms().clear();
			// CompiledShader.Type.VERTEX.getPrograms().clear();
			mc.player.displayClientMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN), true);
		}, mc);
	}

	public static boolean shouldShowName(Entity entity) {
		// var mc = Minecraft.getInstance();
		// return entity instanceof LocalPlayer && mc.isLocalServer() && !mc.options.getCameraType().isFirstPerson() || entity.hasCustomName();
		return !entity.isInvisible() && (entity instanceof LocalPlayer || entity.hasCustomName());
	}

	public static int overrideRenderDistance(int original) {
		return original;
	}
}
