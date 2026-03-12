package dev.latvian.mods.vidlib.integration;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

	MutableObject<DepthOfFieldData> CURRENTLY_APPLIED_DOF = field("CURRENTLY_APPLIED_DOF", new MutableObject<>());

	Supplier<JsonObject> CUSTOM_EDITOR_STATE_DATA = field("CUSTOM_EDITOR_STATE_DATA", JsonObject::new);

	static JsonObject getCustomEditorStateData() {
		return CUSTOM_EDITOR_STATE_DATA.get();
	}
}
