package dev.beast.mods.shimmer.feature.entity;

import com.mojang.datafixers.util.Pair;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Objects;

public class GlobalEntityOverrideEvent extends Event {
	private static void fireEvent() {
		for (var override : EntityOverride.getAllKeys()) {
			override.all = null;
			override.types = null;
			override.filtered = null;
		}

		NeoForge.EVENT_BUS.post(new GlobalEntityOverrideEvent());
	}

	@AutoInit
	public static void fireLoadComplete() {
		fireEvent();
	}

	@AutoInit(AutoInit.Type.DATA_RELOADED)
	public static void fireDataReloaded() {
		fireEvent();
	}

	private GlobalEntityOverrideEvent() {
	}

	public <T> void set(EntityOverride<T> override, EntityFilter filter, EntityOverrideValue<T> value) {
		if (override.filtered == null) {
			override.filtered = new ArrayList<>(1);
		}

		override.filtered.add(Pair.of(filter, Objects.requireNonNull(value)));
	}

	public <T> void set(EntityOverride<T> override, EntityFilter filter, T value) {
		set(override, filter, EntityOverrideValue.fixed(value));
	}

	public <T> void set(EntityOverride<T> override, EntityType<?> type, @Nullable EntityOverrideValue<T> value) {
		if (value != null) {
			if (override.types == null) {
				override.types = new IdentityHashMap<>(1);
			}

			override.types.put(type, value);
		} else if (override.types != null) {
			override.types.remove(type);

			if (override.types.isEmpty()) {
				override.types = null;
			}
		}
	}

	public <T> void set(EntityOverride<T> override, EntityType<?> type, T value) {
		set(override, type, EntityOverrideValue.fixed(value));
	}

	public <T> void set(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value) {
		override.all = value;
	}

	public <T> void set(EntityOverride<T> override, T value) {
		set(override, EntityOverrideValue.fixed(value));
	}
}
