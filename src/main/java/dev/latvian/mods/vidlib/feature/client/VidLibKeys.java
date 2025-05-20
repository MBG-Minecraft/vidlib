package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class VidLibKeys {
	public static KeyMapping freezeTickKeyMapping;
	public static KeyMapping clearParticlesKeyMapping;
	public static KeyMapping reloadKeyMapping;

	private static KeyMapping register(RegisterKeyMappingsEvent event, String name, KeyModifier modifier, int defaultKey) {
		var key = new KeyMapping(name, KeyConflictContext.IN_GAME, modifier, InputConstants.Type.KEYSYM, defaultKey, "key.categories.vidlib");
		event.register(key);
		return key;
	}

	public static void register(RegisterKeyMappingsEvent event) {
		freezeTickKeyMapping = register(event, "key.vidlib.freeze_tick", KeyModifier.NONE, GLFW.GLFW_KEY_P);
		clearParticlesKeyMapping = register(event, "key.vidlib.clear_particles", KeyModifier.NONE, GLFW.GLFW_KEY_L);
		reloadKeyMapping = register(event, "key.vidlib.reload", KeyModifier.NONE, GLFW.GLFW_KEY_R);
	}

	public static void handle(Minecraft mc) {
		while (freezeTickKeyMapping.consumeClick()) {
			if (!mc.player.isReplayCamera()) {
				if (mc.level.tickRateManager().isFrozen()) {
					mc.player.connection.sendCommand("tick unfreeze");
				} else {
					mc.player.connection.sendCommand("tick freeze");
				}
			}
		}

		while (clearParticlesKeyMapping.consumeClick()) {
			mc.level.removeAllParticles();
		}

		while (reloadKeyMapping.consumeClick()) {
			PlayerActionHandler.handle(mc.player, PlayerActionType.RELOAD, true);
		}
	}
}
