package dev.latvian.mods.vidlib.integration;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldData;
import imgui.ImGuiStyle;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

	MutableLong START_TICK = field("START_TICK", new MutableLong(0L));
	MutableLong END_TICK = field("END_TICK", new MutableLong(0L));
	MutableInt TOTAL_DURATION = field("TOTAL_DURATION", new MutableInt(0));

	static long getStartTick() {
		return START_TICK.longValue();
	}

	static long getEndTick() {
		return END_TICK.longValue();
	}

	static int getTotalDuration() {
		return TOTAL_DURATION.intValue();
	}

	List<BiConsumer<List<Packet<? super ClientConfigurationPacketListener>>, List<LongObjectPair<Packet<? super ClientGamePacketListener>>>>> INITIALIZED = listField("INITIALIZED");
	List<Supplier<List<IntObjectPair<ObjectIntPair<String>>>>> MARKERS = listField("MARKERS");
	List<Runnable> CLEANUP = listField("CLEANUP");
	List<Prop> MAKE_PROP_KEYFRAMES = listField("MAKE_PROP_KEYFRAMES");
	List<Consumer<List<Packet<? super ClientConfigurationPacketListener>>>> CONFIG_SNAPSHOT = listField("CONFIG_SNAPSHOT");
	List<Consumer<List<Packet<? super ClientGamePacketListener>>>> GAME_SNAPSHOT = listField("GAME_SNAPSHOT");
	List<BiConsumer<Entity, List<Packet<? super ClientGamePacketListener>>>> ENTITY_SNAPSHOT = listField("ENTITY_SNAPSHOT");
	List<Consumer<ImGraphics>> MENU_BAR = listField("MENU_BAR");
	List<BiConsumer<ImGraphics, Entity>> ENTITY_MENU = listField("ENTITY_MENU");
	List<Consumer<ImGraphics>> VISUALS_MENU = listField("VISUALS_MENU");
	List<Consumer<ImGraphics>> RENDER_FILTER_MENU = listField("RENDER_FILTER_MENU");
	List<Consumer<JsonObject>> EDITOR_STATE_LOADED = listField("EDITOR_STATE_LOADED");
	List<Consumer<JsonObject>> EDITOR_STATE_SAVED = listField("EDITOR_STATE_SAVED");
	List<BiFunction<Vec3, Vec3, HitResult>> CLICK_TARGET = listField("CLICK_TARGET");
	List<Predicate<HitResult>> HANDLE_CLICK_TARGET = listField("HANDLE_CLICK_TARGET");
	List<Consumer<ImGraphics>> POPUPS = listField("POPUPS");
	List<Consumer<CharConsumer>> ICONS = listField("ICONS");
	List<Consumer<ImGuiStyle>> STYLE = listField("STYLE");

	BooleanSupplier IN_REPLAY = booleanField("IN_REPLAY");
	BooleanSupplier IN_EXPORTING = booleanField("IN_EXPORTING");

	MutableObject<DepthOfFieldData> CURRENTLY_APPLIED_DOF = field("CURRENTLY_APPLIED_DOF", new MutableObject<>());

	static boolean isInReplay() {
		return IN_REPLAY.getAsBoolean();
	}

	static boolean isExporting() {
		return IN_EXPORTING.getAsBoolean();
	}

	static boolean isInReplayOrExporting() {
		return isInReplay() || isExporting();
	}

	Supplier<JsonObject> CUSTOM_EDITOR_STATE_DATA = field("CUSTOM_EDITOR_STATE_DATA", JsonObject::new);

	static JsonObject getCustomEditorStateData() {
		return CUSTOM_EDITOR_STATE_DATA.get();
	}

	BooleanSupplier RENDER_BLOCKS = booleanField("RENDER_BLOCKS");
	BooleanSupplier RENDER_ENTITIES = booleanField("RENDER_ENTITIES");
	BooleanSupplier RENDER_PLAYERS = booleanField("RENDER_PLAYERS");
	BooleanSupplier RENDER_PARTICLES = booleanField("RENDER_PARTICLES");
	BooleanSupplier RENDER_NAMETAGS = booleanField("RENDER_NAMETAGS");
	Predicate<UUID> IS_ENTITY_HIDDEN = field("IS_ENTITY_HIDDEN", uuid -> false);
	Predicate<UUID> IS_NAME_HIDDEN = field("IS_NAME_HIDDEN", uuid -> false);
	Predicate<UUID> IS_HEALTH_HIDDEN = field("IS_HEALTH_HIDDEN", uuid -> false);

	static boolean getRenderBlocks() {
		return RENDER_BLOCKS.getAsBoolean();
	}

	static boolean getRenderEntities() {
		return RENDER_ENTITIES.getAsBoolean();
	}

	static boolean getRenderPlayers() {
		return RENDER_PLAYERS.getAsBoolean();
	}

	static boolean getRenderParticles() {
		return RENDER_PARTICLES.getAsBoolean();
	}

	static boolean getRenderNameTags() {
		return RENDER_NAMETAGS.getAsBoolean();
	}

	static boolean isEntityHidden(UUID uuid) {
		return IS_ENTITY_HIDDEN.test(uuid);
	}

	static boolean isNameHidden(UUID uuid) {
		return IS_NAME_HIDDEN.test(uuid);
	}

	static boolean isHealthHidden(UUID uuid) {
		return IS_HEALTH_HIDDEN.test(uuid);
	}
}
