package dev.latvian.mods.vidlib.feature.data;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DataMapOverrides {
	public static DataMapOverrides INSTANCE;

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
		private final DataEntry[][] sortedEntries;
		private boolean hasAny;

		public DataMap(DataKeyStorage storage) {
			this.sortedEntries = new DataEntry[storage.all.size()][];
			this.hasAny = false;
		}

		private void update(Map<DataKey<?>, Long2ObjectMap<DataEntry>> map) {
			Arrays.fill(sortedEntries, null);
			hasAny = false;

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

				if (!entries.isEmpty()) {
					sortedEntries[key.index()] = entries.reversed().toArray(new DataEntry[0]);
					hasAny = true;
				}
			}
		}

		@Nullable
		public <T> T getOverride(DataKey<T> type, long time) {
			if (hasAny) {
				var array = sortedEntries[type.index()];

				if (array != null) {
					for (var entry : array) {
						if (time >= entry.time) {
							return (T) entry.value;
						}
					}
				}
			}

			return null;
		}
	}

	private static final Function<UUID, DataMap> PLAYERS = k -> new DataMap(DataKey.PLAYER);

	public final DataMap serverData;
	public final Map<UUID, DataMap> playerData;

	public DataMapOverrides() {
		this.serverData = new DataMap(DataKey.SERVER);
		this.playerData = new Object2ObjectOpenHashMap<>();
	}

	public DataMap getPlayerMap(UUID player) {
		return playerData.computeIfAbsent(player, PLAYERS);
	}
}