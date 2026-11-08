package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.klib.client.KLibKeys;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.sdl.SDLScancode;

public class VidLibKeys {
	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(ID.vidlib("vidlib"));

	public static KeyMapping freezeTickKeyMapping;
	public static KeyMapping clearParticlesKeyMapping;
	public static KeyMapping reloadKeyMapping;
	public static KeyMapping repeatLastCommandKeyMapping;
	public static KeyMapping reloadShadersKeyMapping;
	public static KeyMapping playerGlowKeyMapping;
	public static KeyMapping capturePanoramaKeyMapping;

	private static KeyMapping register(RegisterKeyMappingsEvent event, String name, KeyModifier modifier, int defaultKey, KeyConflictContext conflict) {
		var key = new KeyMapping(name, conflict, modifier, InputConstants.Type.KEYBOARD.getOrCreate(defaultKey), CATEGORY);
		event.register(key);
		return key;
	}

	private static KeyMapping register(RegisterKeyMappingsEvent event, String name, KeyModifier modifier, int defaultKey) {
		return register(event, name, modifier, defaultKey, KeyConflictContext.IN_GAME);
	}

	public static void register(RegisterKeyMappingsEvent event) {
		event.registerCategory(CATEGORY);

		freezeTickKeyMapping = register(event, "key.vidlib.freeze_tick", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_UNKNOWN);
		clearParticlesKeyMapping = register(event, "key.vidlib.clear_particles", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_UNKNOWN);
		reloadKeyMapping = register(event, "key.vidlib.reload", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_R);
		repeatLastCommandKeyMapping = register(event, "key.vidlib.repeat_last_command", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_UNKNOWN);
		reloadShadersKeyMapping = register(event, "key.vidlib.reload_shaders", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_UNKNOWN);
		playerGlowKeyMapping = register(event, "key.vidlib.player_glow", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_GRAVE);
		capturePanoramaKeyMapping = register(event, "key.vidlib.capture_panorama", KeyModifier.NONE, SDLScancode.SDL_SCANCODE_UNKNOWN);
	}

	public static void handle(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			return;
		}

		while (freezeTickKeyMapping.consumeClick()) {
			if (!ClientSessionData.of(mc).isReplay) {
				if (mc.level.tickRateManager().isFrozen()) {
					mc.runClientCommand("tick unfreeze");
				} else {
					mc.runClientCommand("tick freeze");
				}
			}
		}

		while (clearParticlesKeyMapping.consumeClick()) {
			mc.level.removeAllParticles();
		}

		while (reloadKeyMapping.consumeClick()) {
			PlayerActionHandler.handle(mc.player, PlayerActionType.RELOAD, true);
		}

		while (repeatLastCommandKeyMapping.consumeClick()) {
			var recent = mc.gui.hud.getChat().getRecentChat();

			if (!recent.isEmpty()) {
				mc.runClientCommand(recent.getLast());
			}
		}

		while (capturePanoramaKeyMapping.consumeClick()) {
			if (!ClientGameEngine.DISABLE_IMGUI) {
				mc.player.sendSystemMessage(Component.literal("You must have env DISABLE_IMGUI set to true").withColor(Color.RED.argb()));
			} else {
				mc.player.sendSystemMessage(mc.grabPanoramixScreenshot(FMLPaths.GAMEDIR.get().toFile()));
			}
		}

		while (KLibKeys.increaseKeyMapping.consumeClick()) {
			ClientGameEngine.INSTANCE.handleIncreaseKey(mc);
		}

		while (KLibKeys.decreaseKeyMapping.consumeClick()) {
			ClientGameEngine.INSTANCE.handleDecreaseKey(mc);
		}
	}

	public static boolean handleDebugKeys(Minecraft mc, int key) {
		if (key == reloadShadersKeyMapping.getDefaultKey().getValue()) {
			MiscClientUtils.reloadShaders(mc);
			return true;
		}

		return false;
	}
}
