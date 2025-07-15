package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface FlashbackIntegration {
	Lazy<Class<?>> FLASHBACK_API = Lazy.of(() -> {
		try {
			return Class.forName("com.moulberry.flashback.compat.FlashbackAPI");
		} catch (ClassNotFoundException e) {
			return null;
		}
	});

	private static <T> Lazy<List<T>> field(String name) {
		return FLASHBACK_API.map(c -> {
			try {
				var field = c.getDeclaredField(name);
				var value = field.get(null);
				return (List<T>) value;
			} catch (Exception ex) {
				return new ArrayList<>();
			}
		});
	}

	Lazy<List<Consumer<List<Packet<? super ClientConfigurationPacketListener>>>>> CONFIG_SNAPSHOT = field("CONFIG_SNAPSHOT");
	Lazy<List<Consumer<List<Packet<? super ClientGamePacketListener>>>>> GAME_SNAPSHOT = field("GAME_SNAPSHOT");
	Lazy<List<BiConsumer<Entity, List<Packet<? super ClientGamePacketListener>>>>> ENTITY_SNAPSHOT = field("ENTITY_SNAPSHOT");
	Lazy<List<Consumer<Entity>>> ENTITY_MENU = field("ENTITY_MENU");
	Lazy<List<Runnable>> VISUALS_MENU = field("VISUALS_MENU");
	Lazy<List<Runnable>> RENDER_FILTER_MENU = field("RENDER_FILTER_MENU");

	static void init() {
		VidLib.LOGGER.info("Flashback integration loaded");
		CONFIG_SNAPSHOT.get().add(FlashbackIntegration::configSnapshot);
		GAME_SNAPSHOT.get().add(FlashbackIntegration::gameSnapshot);
		ENTITY_SNAPSHOT.get().add(FlashbackIntegration::entitySnapshot);
		ENTITY_MENU.get().add(FlashbackIntegration::entityMenu);
		VISUALS_MENU.get().add(FlashbackIntegration::visualsMenu);
		RENDER_FILTER_MENU.get().add(FlashbackIntegration::renderFilterMenu);
	}

	private static void configSnapshot(List<Packet<? super ClientConfigurationPacketListener>> packets) {
		// VidLib.LOGGER.info("Flashback Config snapshot");
	}

	private static void gameSnapshot(List<Packet<? super ClientGamePacketListener>> packets) {
		VidLib.LOGGER.info("Flashback Game snapshot");

		var mc = Minecraft.getInstance();
		var packets2 = new S2CPacketBundleBuilder(mc.level);
		mc.player.vl$sessionData().sync(packets2, mc.player, 1);
		packets2.sendUnbundled(packets::add);
	}

	private static void entitySnapshot(Entity entity, List<Packet<? super ClientGamePacketListener>> packets) {
		var packets2 = new S2CPacketBundleBuilder(Minecraft.getInstance().level);
		entity.replaySnapshot(packets2);
		packets2.sendUnbundled(packets::add);
	}

	private static void entityMenu(Entity entity) {
	}

	private static void visualsMenu() {
	}

	private static void renderFilterMenu() {
	}
}
