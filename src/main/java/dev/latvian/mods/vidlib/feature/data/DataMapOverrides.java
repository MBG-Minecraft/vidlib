package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class DataMapOverrides {
	public static DataMapOverrides INSTANCE;
	public static final DataKey<Set<String>> PLAYER_TAGS = DataKey.PLAYER.builder("player_tags", DataTypes.STRING.setOf(), Set.of()).buildDummy();

	public record DataEntry(long time, Object value) implements Comparable<DataEntry> {
		@Override
		public int compareTo(@NotNull DataMapOverrides.DataEntry o) {
			return Long.compareUnsigned(time, o.time);
		}
	}

	public static class Builder {
		private final Map<DataKey<?>, Long2ObjectMap<DataEntry>> server = new Reference2ObjectOpenHashMap<>();
		private final Map<UUID, Map<DataKey<?>, Long2ObjectMap<DataEntry>>> players = new Object2ObjectOpenHashMap<>();

		public void set(long time, @Nullable UUID uuid, DataKey<?> key, Object value) {
			var dataMap = uuid == null ? server : players.computeIfAbsent(uuid, k -> new Reference2ObjectOpenHashMap<>());
			var entries = dataMap.computeIfAbsent(key, k -> new Long2ObjectOpenHashMap<>());

			if (entries.isEmpty()) {
				entries.put(0L, new DataEntry(0L, key.defaultValue()));
			}

			entries.put(time, new DataEntry(time, value));
		}

		public DataMapOverrides build() {
			var overrides = new DataMapOverrides();
			overrides.serverData.update(server);

			for (var entry : players.entrySet()) {
				overrides.getPlayerMap(entry.getKey()).update(entry.getValue());
			}

			return overrides;
		}
	}

	public static class DataMap {
		private final Map<DataKey<?>, List<DataEntry>> sortedEntries = new Reference2ObjectOpenHashMap<>();

		private void update(Map<DataKey<?>, Long2ObjectMap<DataEntry>> map) {
			sortedEntries.clear();

			for (var entry : map.entrySet()) {
				var key = entry.getKey();
				var entries = new ArrayList<DataEntry>();
				var entries0 = new ArrayList<>(entry.getValue().values());
				entries0.sort(null);

				for (var e : entries0) {
					if (entries.isEmpty() || !Objects.equals(e.value, entries.getLast().value)) {
						entries.add(e);
					}
				}

				sortedEntries.put(key, List.copyOf(entries.reversed()));
			}
		}

		@Nullable
		public <T> T getOverride(DataKey<T> type, long time) {
			if (sortedEntries.isEmpty()) {
				return null;
			}

			var list = sortedEntries.get(type);

			if (list == null || list.isEmpty()) {
				return null;
			}

			for (var entry : list) {
				if (time >= entry.time) {
					return (T) entry.value;
				}
			}

			return null;
		}
	}

	private static final Function<UUID, DataMap> PLAYERS = k -> new DataMap();

	public final DataMap serverData;
	public final Map<UUID, DataMap> playerData;

	public DataMapOverrides() {
		this.serverData = new DataMap();
		this.playerData = new Object2ObjectOpenHashMap<>();
	}

	public DataMap getPlayerMap(UUID player) {
		return playerData.computeIfAbsent(player, PLAYERS);
	}
}