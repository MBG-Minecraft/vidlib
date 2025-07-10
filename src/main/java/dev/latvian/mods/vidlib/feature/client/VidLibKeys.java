package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ArrayListDeque;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class VidLibKeys {
	public static KeyMapping freezeTickKeyMapping;
	public static KeyMapping clearParticlesKeyMapping;
	public static KeyMapping reloadKeyMapping;
	public static KeyMapping repeatLastCommandKeyMapping;
	public static KeyMapping adminPanelKeyMapping;

	private static KeyMapping register(RegisterKeyMappingsEvent event, String name, KeyModifier modifier, int defaultKey, KeyConflictContext conflict) {
		var key = new KeyMapping(name, conflict, modifier, InputConstants.Type.KEYSYM, defaultKey, "key.categories.vidlib");
		event.register(key);
		return key;
	}

	private static KeyMapping register(RegisterKeyMappingsEvent event, String name, KeyModifier modifier, int defaultKey) {
		return register(event, name, modifier, defaultKey, KeyConflictContext.IN_GAME);
	}

	public static void register(RegisterKeyMappingsEvent event) {
		freezeTickKeyMapping = register(event, "key.vidlib.freeze_tick", KeyModifier.NONE, GLFW.GLFW_KEY_P);
		clearParticlesKeyMapping = register(event, "key.vidlib.clear_particles", KeyModifier.NONE, GLFW.GLFW_KEY_L);
		reloadKeyMapping = register(event, "key.vidlib.reload", KeyModifier.NONE, GLFW.GLFW_KEY_R);
		repeatLastCommandKeyMapping = register(event, "key.vidlib.repeat_last_command", KeyModifier.NONE, GLFW.GLFW_KEY_SEMICOLON);
		adminPanelKeyMapping = register(event, "key.vidlib.admin_panel", KeyModifier.NONE, GLFW.GLFW_KEY_MENU, KeyConflictContext.UNIVERSAL);
	}

	public static void handle(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			return;
		}

		while (freezeTickKeyMapping.consumeClick()) {
			if (!mc.player.isReplayCamera()) {
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
			if (!mc.commandHistory().history().isEmpty()) {
				mc.runClientCommand(((ArrayListDeque<String>) mc.commandHistory().history()).getLast());
			}
		}
	}
}
