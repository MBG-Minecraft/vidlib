package dev.latvian.mods.vidlib.feature.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.util.Cast;
import dev.latvian.mods.vidlib.util.JsonUtils;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class DataRecorder {
	public static final DataType<Set<String>> PLAYER_TAGS = DataType.PLAYER.builder("player_tags", KnownCodec.STRING.setOf(), Set.of()).buildDummy();

	public static class DataMap {
		public record DataEntry(long time, Object value) {
		}

		private static final Function<DataType<?>, Long2ObjectLinkedOpenHashMap<Object>> TYPES = k -> new Long2ObjectLinkedOpenHashMap<>(1);

		private final Map<DataType<?>, Long2ObjectLinkedOpenHashMap<Object>> map = new Reference2ObjectOpenHashMap<>(1);
		private final Map<DataType<?>, List<DataEntry>> sortedEntries = new Reference2ObjectOpenHashMap<>();

		public void set(long time, DataType<?> type, Object value) {
			var types = map.computeIfAbsent(type, TYPES);

			if (types.isEmpty()) {
				types.put(0L, type.defaultValue());
			}

			types.put(time, value);
			sortedEntries.remove(type);
		}

		private void load(DynamicOps<JsonElement> ops, DataTypeStorage storage, JsonObject json) {
			for (var entry : json.entrySet()) {
				var id = ResourceLocation.parse(entry.getKey());

				var type = id.equals(PLAYER_TAGS.id()) ? PLAYER_TAGS : storage.all.get(id);

				if (type == null) {
					VidLib.LOGGER.warn("Data type with id " + entry.getKey() + " not found");
					continue;
				}

				var valueJson = entry.getValue().getAsJsonObject();

				for (var timeEntry : valueJson.entrySet()) {
					var time = Long.parseUnsignedLong(timeEntry.getKey());
					var value = type.type().codec().parse(ops, timeEntry.getValue()).getOrThrow();
					set(time, type, value);
				}
			}
		}

		private JsonObject save(DynamicOps<JsonElement> ops) {
			var json = new JsonObject();

			for (var dataEntry : map.entrySet()) {
				var valueJson = new JsonObject();
				JsonElement lastValue = null;

				for (var timeEntry : dataEntry.getValue().long2ObjectEntrySet().stream().sorted((e1, e2) -> Long.compareUnsigned(e1.getLongKey(), e2.getLongKey())).toList()) {
					var time = Long.toUnsignedString(timeEntry.getLongKey());
					var value = dataEntry.getKey().type().codec().encodeStart(ops, Cast.to(timeEntry.getValue())).getOrThrow();

					if (lastValue == null || !Objects.equals(lastValue, value)) {
						valueJson.add(time, value);
						lastValue = value;
					}
				}

				json.add(dataEntry.getKey().id(), valueJson);
			}

			return json;
		}

		@Nullable
		public <T> T getOverride(DataType<T> type, long time) {
			var list = sortedEntries.get(type);

			if (list == null) {
				list = new ArrayList<>(1);

				var timeMap = map.get(type);

				if (timeMap != null && !timeMap.isEmpty()) {
					for (var entry : timeMap.long2ObjectEntrySet()) {
						list.add(new DataEntry(entry.getLongKey(), entry.getValue()));
					}

					if (list.size() >= 2) {
						list.sort((e1, e2) -> Long.compareUnsigned(e1.time, e2.time));
						list = list.reversed();
					}
				}

				sortedEntries.put(type, List.copyOf(list));
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

	public final boolean record;
	public final long start;
	public final DataMap serverData;
	public final Map<UUID, DataMap> playerData;

	public DataRecorder(boolean record, long start) {
		this.record = record;
		this.start = start;
		this.serverData = new DataMap();
		this.playerData = new Object2ObjectOpenHashMap<>();
	}

	public void load(DynamicOps<JsonElement> ops, Path path) {
		if (Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				var json = JsonUtils.read(reader);
				load(ops, json.getAsJsonObject());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void load(DynamicOps<JsonElement> ops, JsonObject json) {
		if (json.has("server")) {
			serverData.load(ops, DataType.SERVER, json.getAsJsonObject("server"));
		}

		if (json.has("player")) {
			for (var entry : json.getAsJsonObject("player").entrySet()) {
				var player = UndashedUuid.fromString(entry.getKey());
				var types = playerData.computeIfAbsent(player, PLAYERS);
				types.load(ops, DataType.PLAYER, entry.getValue().getAsJsonObject());
			}
		}
	}

	public JsonObject save(DynamicOps<JsonElement> ops) {
		var json = new JsonObject();

		json.add("server", serverData.save(ops));

		var playerJson = new JsonObject();

		for (var entry : playerData.entrySet()) {
			playerJson.add(UndashedUuid.toString(entry.getKey()), entry.getValue().save(ops));
		}

		json.add("player", playerJson);

		return json;
	}

	public void setServer(long time, DataType<?> type, Object value) {
		serverData.set(time, type, value);
	}

	public DataMap getPlayerMap(UUID player) {
		return playerData.computeIfAbsent(player, PLAYERS);
	}

	public void setPlayer(long time, UUID player, DataType<?> type, Object value) {
		getPlayerMap(player).set(time, type, value);
	}
}