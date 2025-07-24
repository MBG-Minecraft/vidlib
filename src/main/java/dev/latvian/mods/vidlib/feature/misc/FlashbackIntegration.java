package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Lazy;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface FlashbackIntegration {
	Lazy<Class<?>> FLASHBACK_API = Lazy.of(() -> {
		try {
			return Class.forName("com.moulberry.flashback.compat.FlashbackAPI");
		} catch (ClassNotFoundException e) {
			return null;
		}
	});

	private static <T> T field(String name, @Nullable T defaultValue) {
		try {
			var c = FLASHBACK_API.get();
			var field = c.getDeclaredField(name);
			var value = field.get(null);
			return (T) value;
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	private static BooleanSupplier booleanField(String name) {
		return field(name, () -> false);
	}

	private static <T> List<T> listField(String name) {
		return field(name, new ArrayList<>(1));
	}

	List<BiConsumer<Entity, List<Packet<? super ClientGamePacketListener>>>> ENTITY_SNAPSHOT = listField("ENTITY_SNAPSHOT");
	List<Consumer<List<Packet<? super ClientConfigurationPacketListener>>>> CONFIG_SNAPSHOT = listField("CONFIG_SNAPSHOT");
	List<Consumer<List<Packet<? super ClientGamePacketListener>>>> GAME_SNAPSHOT = listField("GAME_SNAPSHOT");
	List<Consumer<Entity>> ENTITY_MENU = listField("ENTITY_MENU");
	List<Runnable> VISUALS_MENU = listField("VISUALS_MENU");
	List<Runnable> RENDER_FILTER_MENU = listField("RENDER_FILTER_MENU");
	List<BiFunction<Vec3, Vec3, HitResult>> CLICK_TARGET = listField("CLICK_TARGET");
	List<Predicate<HitResult>> HANDLE_CLICK_TARGET = listField("HANDLE_CLICK_TARGET");
	List<Runnable> POPUPS = listField("POPUPS");

	BooleanSupplier IN_REPLAY = booleanField("IN_REPLAY");
	BooleanSupplier IN_EXPORTING = booleanField("IN_EXPORTING");

	Supplier<JsonObject> CUSTOM_EDITOR_STATE_DATA = field("CUSTOM_EDITOR_STATE_DATA", JsonObject::new);

	BooleanSupplier RENDER_BLOCKS = booleanField("RENDER_BLOCKS");
	BooleanSupplier RENDER_ENTITIES = booleanField("RENDER_ENTITIES");
	BooleanSupplier RENDER_PLAYERS = booleanField("RENDER_PLAYERS");
	BooleanSupplier RENDER_PARTICLES = booleanField("RENDER_PARTICLES");
	BooleanSupplier RENDER_NAMETAGS = booleanField("RENDER_NAMETAGS");

	static JsonObject getCustomEditorStateData() {
		return CUSTOM_EDITOR_STATE_DATA.get();
	}
}
