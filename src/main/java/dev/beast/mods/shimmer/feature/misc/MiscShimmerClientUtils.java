package dev.beast.mods.shimmer.feature.misc;

import com.google.gson.JsonObject;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.util.JsonUtils;
import dev.beast.mods.shimmer.util.Lazy;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class MiscShimmerClientUtils {
	public static final ContextKey<Boolean> CREATIVE = new ContextKey<>(Shimmer.id("creative"));
	public static final ContextKey<Clothing> CLOTHING = new ContextKey<>(Shimmer.id("clothing"));

	public static KeyMapping freezeTickKeyMapping;

	public static FogParameters fogOverride = FogParameters.NO_FOG;

	public static final Lazy<JsonObject> KEYBINDS = Lazy.of(() -> {
		var path = Shimmer.HOME_DIR.get().resolve("keybinds.json");

		if (Files.exists(path)) {
			try (var reader = Files.newBufferedReader(Shimmer.HOME_DIR.get().resolve("keybinds.json"))) {
				return JsonUtils.read(reader).getAsJsonObject();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return new JsonObject();
	});

	@AutoInit(AutoInit.Type.CLIENT_OPTIONS_SAVED)
	public static void saveKeybinds(Options options) {
		var json = KEYBINDS.get();

		for (var key : options.keyMappings) {
			json.addProperty(key.getName(), key.saveString() + (key.getKeyModifier() != KeyModifier.NONE ? ":" + key.getKeyModifier() : ""));
		}

		try (var writer = Files.newBufferedWriter(Shimmer.HOME_DIR.get().resolve("keybinds.json"))) {
			JsonUtils.write(writer, json, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

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
}
