package dev.latvian.mods.vidlib.integration.replay;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.pin.Pin;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.integration.FlashbackIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class VLFlashbackIntegration {
	public static void init() {
		VidLib.LOGGER.info("Flashback integration loaded");
		FlashbackIntegration.GAME_SNAPSHOT.add(VLFlashbackIntegration::gameSnapshot);
		FlashbackIntegration.ENTITY_SNAPSHOT.add(VLFlashbackIntegration::entitySnapshot);
		FlashbackIntegration.EDITOR_STATE_LOADED.add(VLFlashbackIntegration::editorStateLoaded);
		FlashbackIntegration.EDITOR_STATE_SAVED.add(VLFlashbackIntegration::editorStateSaved);
	}

	private static void gameSnapshot(VLS2CPacketConsumer packets) {
		VidLib.LOGGER.info("Flashback Game snapshot");

		var mc = Minecraft.getInstance();
		var session = mc.player.vl$sessionData();
		var packets2 = new S2CPacketBundleBuilder(mc.level);
		session.sync(packets2, mc.player, 1);

		if (!session.markers.isEmpty()) {
			session.markers.forEach(packets2::s2c);
		}

		packets2.sendUnbundled(packets);
	}

	private static void entitySnapshot(Entity entity, VLS2CPacketConsumer packets) {
		var packets2 = new S2CPacketBundleBuilder(Minecraft.getInstance().level);
		entity.replaySnapshot(packets2);
		packets2.sendUnbundled(packets);
	}

	private static void editorStateLoaded(JsonObject customData) {
		Pins.PINS.clear();

		if (customData.has("vidlib:pins")) {
			var pins = customData.getAsJsonArray("vidlib:pins");

			for (var e : pins) {
				if (e instanceof JsonObject pinJson) {
					try {
						var uuid = UndashedUuid.fromStringLenient(pinJson.get("uuid").getAsString());
						var pin = Pin.CODEC.parse(JsonOps.INSTANCE, pinJson).getOrThrow();
						Pins.PINS.put(uuid, pin);
					} catch (Throwable t) {
						VidLib.LOGGER.error("Failed to load player pin from editor state", t);
					}
				}
			}
		}
	}

	private static void editorStateSaved(JsonObject customData) {
		if (!Pins.PINS.isEmpty()) {
			var pins = new JsonArray();

			for (var entry : Pins.PINS.entrySet()) {
				var json = new JsonObject();
				json.addProperty("uuid", UndashedUuid.toString(entry.getKey()));
				Pin.CODEC.encode(entry.getValue(), JsonOps.INSTANCE, json).ifSuccess(pins::add);
			}

			customData.add("vidlib:pins", pins);
		}
	}
}
