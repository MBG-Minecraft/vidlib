package dev.beast.mods.shimmer.feature.misc;

import com.mojang.blaze3d.shaders.Program;
import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.InactiveProfiler;

import java.util.concurrent.CompletableFuture;

public class MiscShimmerClientUtils {
	public static boolean handleDebugKeys(Minecraft mc, int key) {
		if (key == ShimmerConfig.cycleShadersKey) {
			if (Screen.hasShiftDown()) {
				mc.execute(mc.gameRenderer::shutdownEffect);
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
		Util.backgroundExecutor().submit(() -> {
			try {
				mc.gameRenderer.createReloadListener().reload(CompletableFuture::completedFuture, mc.getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, Util.backgroundExecutor(), mc).get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			mc.submit(() -> {
				mc.levelRenderer.onResourceManagerReload(mc.getResourceManager());
				Program.Type.FRAGMENT.getPrograms().clear();
				Program.Type.VERTEX.getPrograms().clear();
				mc.player.displayClientMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN), true);
			});
		});
	}

	public static boolean canSeeSpectators(AbstractClientPlayer player) {
		// TODO
		return false;
	}
}
