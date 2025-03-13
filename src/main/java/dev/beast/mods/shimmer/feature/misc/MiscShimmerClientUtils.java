package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.context.ContextKey;

public interface MiscShimmerClientUtils {
	ContextKey<Boolean> CREATIVE = new ContextKey<>(Shimmer.id("creative"));

	static boolean handleDebugKeys(Minecraft mc, int key) {
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

	static void reloadShaders(Minecraft mc) {
		/* FIXME
		Util.backgroundExecutor().execute(() -> {
			try {
				mc.gameRenderer.createReloadListener().reload(CompletableFuture::completedFuture, mc.getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, Util.backgroundExecutor(), mc).get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			mc.submit(() -> {
				mc.levelRenderer.onResourceManagerReload(mc.getResourceManager());
				CompiledShader.Type.FRAGMENT.getPrograms().clear();
				CompiledShader.Type.VERTEX.getPrograms().clear();
				mc.player.displayClientMessage(Component.literal("Shaders reloaded!").withStyle(ChatFormatting.GREEN), true);
			});
		});
		 */
	}

	static boolean canSeeSpectators(AbstractClientPlayer player) {
		// TODO
		return false;
	}
}
