package dev.latvian.mods.vidlib.integration;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

	private static <T> List<T> listField(String name) {
		return field(name, new ArrayList<>(1));
	}

	List<Prop> MAKE_PROP_KEYFRAMES = listField("MAKE_PROP_KEYFRAMES");
	List<Consumer<List<Packet<? super ClientConfigurationPacketListener>>>> CONFIG_SNAPSHOT = listField("CONFIG_SNAPSHOT");
	List<Consumer<VLS2CPacketConsumer>> GAME_SNAPSHOT = listField("GAME_SNAPSHOT");
	List<BiConsumer<Entity, VLS2CPacketConsumer>> ENTITY_SNAPSHOT = listField("ENTITY_SNAPSHOT");
	List<Consumer<JsonObject>> EDITOR_STATE_LOADED = listField("EDITOR_STATE_LOADED");
	List<Consumer<JsonObject>> EDITOR_STATE_SAVED = listField("EDITOR_STATE_SAVED");

	MutableObject<DepthOfFieldData> CURRENTLY_APPLIED_DOF = field("CURRENTLY_APPLIED_DOF", new MutableObject<>());

	Supplier<JsonObject> CUSTOM_EDITOR_STATE_DATA = field("CUSTOM_EDITOR_STATE_DATA", JsonObject::new);

	static JsonObject getCustomEditorStateData() {
		return CUSTOM_EDITOR_STATE_DATA.get();
	}
}
