package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
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
		private final Map<DataKey<GlobalSessionData, ?>, Long2ObjectMap<DataEntry>> server = new Reference2ObjectOpenHashMap<>();
		private final Map<ResourceKey<Level>, Map<DataKey<LevelSessionData<?>, ?>, Long2ObjectMap<DataEntry>>> levels = new Object2ObjectOpenHashMap<>();
		private final Map<UUID, Map<DataKey<PlayerSessionData, ?>, Long2ObjectMap<DataEntry>>> players = new Object2ObjectOpenHashMap<>();

		public void setServer(long time, DataKey<GlobalSessionData, ?> key, Object value) {
			set(time, server, key, value);
		}

		public void setLevel(long time, ResourceKey<Level> dimension, DataKey<LevelSessionData<?>, ?> key, Object value) {
			set(time, levels.computeIfAbsent(dimension, new Reference2ObjectOpenHashMap<>()), key, value);
		}

		public void setPlayer(long time, UUID player, DataKey<PlayerSessionData, ?> key, Object value) {
			set(time, players.computeIfAbsent(player, k -> new Object2ObjectOpenHashMap<>()), key, value);
		}

		public <K> void set(long time, Map<DataKey<K, ?>, Long2ObjectMap<DataEntry>> dataMap, DataKey<K, ?> key, Object value) {
			var entries = dataMap.computeIfAbsent(key, k -> new Long2ObjectOpenHashMap<>());

			if (entries.isEmpty()) {
				entries.put(0L, new DataEntry(0L, key.defaultValue));
			}

			entries.put(time, new DataEntry(time, value));
		}

		public DataMapOverrides build() {
			var overrides = new DataMapOverrides();
			overrides.serverData.update(server);

			for (var entry : levels.entrySet()) {
				overrides.getLevelMap(entry.getKey()).update(entry.getValue());
			}

			for (var entry : players.entrySet()) {
				overrides.getPlayerMap(entry.getKey()).update(entry.getValue());
			}

			return overrides;
		}
	}

	public static class OverrideDataMap<K> {
		private final DataEntry[][] sortedEntries;
		private boolean hasAny;

		public OverrideDataMap(DataKeyStorage<K> storage) {
			this.sortedEntries = new DataEntry[storage.all.size()][];
			this.hasAny = false;
		}

		private void update(Map<DataKey<K, ?>, Long2ObjectMap<DataEntry>> map) {
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
					sortedEntries[key.index] = entries.reversed().toArray(new DataEntry[0]);
					hasAny = true;
				}
			}
		}

		@Nullable
		public <T> T getOverride(DataKey<K, T> type, long time) {
			if (hasAny) {
				var array = sortedEntries[type.index];

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

	private static final Function<ResourceKey<Level>, OverrideDataMap<LevelSessionData<?>>> LEVELS = _ -> new OverrideDataMap<>(DataKey.LEVEL);
	private static final Function<UUID, OverrideDataMap<PlayerSessionData>> PLAYERS = _ -> new OverrideDataMap<>(DataKey.PLAYER);

	public final OverrideDataMap<GlobalSessionData> serverData;
	public final Map<ResourceKey<Level>, OverrideDataMap<LevelSessionData<?>>> levelData;
	public final Map<UUID, OverrideDataMap<PlayerSessionData>> playerData;

	public DataMapOverrides() {
		this.serverData = new OverrideDataMap<>(DataKey.GLOBAL);
		this.levelData = new Reference2ObjectOpenHashMap<>();
		this.playerData = new Object2ObjectOpenHashMap<>();
	}

	public OverrideDataMap<LevelSessionData<?>> getLevelMap(ResourceKey<Level> dimension) {
		return levelData.computeIfAbsent(dimension, LEVELS);
	}

	public OverrideDataMap<PlayerSessionData> getPlayerMap(UUID player) {
		return playerData.computeIfAbsent(player, PLAYERS);
	}
}