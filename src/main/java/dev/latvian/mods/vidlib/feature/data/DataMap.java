package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.LevelGameTimeProvider;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class DataMap<K> {
	public final LevelGameTimeProvider worldSession;
	public final K key;
	public final DataKeyStorage<K> storage;
	private final TrackedDataMapValue<K>[] map;
	public DataMapOverrides.OverrideDataMap<K> overrides;
	private Optional<Object>[] superOverrides;

	public DataMap(GlobalSessionData worldSession, K key, DataKeyStorage<K> storage) {
		this.worldSession = worldSession;
		this.key = key;
		this.storage = storage;
		this.map = new TrackedDataMapValue[storage.all.size()];
		this.overrides = null;
		this.superOverrides = null;

		for (var type : storage.all.values()) {
			var value = new TrackedDataMapValue<>(type);
			value.data = type.defaultValue;
			this.map[type.index] = value;
		}
	}

	TrackedDataMapValue<K> init(DataKey<K, ?> type) {
		return map[type.index];
	}

	@SuppressWarnings("unchecked")
	public <T> T getActual(DataKey<K, T> type) {
		return (T) init(type).data;
	}

	private void checkKey(DataKey<K, ?> type) {
		if (storage != type.storage) {
			throw new IllegalArgumentException("Tried to use " + type.storage + " key in " + storage + " DataMap");
		}
	}

	public <T> T get(DataKey<K, T> type) {
		checkKey(type);

		if (superOverrides != null) {
			var v = superOverrides[type.index];

			if (v != null) {
				return Cast.to(v.orElse(null));
			}
		}

		if (overrides != null) {
			var v = overrides.getOverride(type, worldSession.getLevelGameTime());

			if (v != null) {
				return v;
			}
		}

		return getActual(type);
	}

	@Nullable
	public <T> T getOptional(DataKey<K, T> type) {
		return get(type);
	}

	public <T> void set(DataKey<K, T> type, @Nullable T value) {
		checkKey(type);

		var v = init(type);
		v.data = value;
		v.setChanged();
	}

	public <T> void reset(DataKey<K, T> type) {
		set(type, type.defaultValue);
	}

	public void setSuperOverride(DataKey<K, ?> type, @Nullable Object value) {
		checkKey(type);

		if (superOverrides == null) {
			superOverrides = new Optional[map.length];
		}

		superOverrides[type.index] = Optional.ofNullable(value);
	}

	public void removeSuperOverride(DataKey<K, ?> type) {
		if (superOverrides != null && storage == type.storage) {
			superOverrides[type.index] = null;

			for (var value : superOverrides) {
				if (value != null) {
					return;
				}
			}

			superOverrides = null;
		}
	}

	public boolean hasSuperOverride(DataKey<K, ?> type) {
		return superOverrides != null && storage == type.storage && superOverrides[type.index] != null;
	}

	public void load(MinecraftServer server, Path path) {
		for (var v : map) {
			v.data = v.dataKey.defaultValue;
		}

		if (Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var data = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
				var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

				for (var type : storage.saved.values()) {
					var tag = data.get(type.id);

					if (tag != null) {
						try {
							var dataValue = type.type.codec().parse(ops, tag).getOrThrow();

							if (dataValue != null) {
								init(type).data = dataValue;
							}
						} catch (Exception ex) {
							VidLib.LOGGER.error("Failed to parse '" + type + "' from " + tag, ex);
						}
					}
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to load data from " + path, ex);
			}
		}
	}

	public void save(MinecraftServer server, Path path) {
		boolean needsSave = false;

		for (var v : map) {
			if (v.save != v.changeCount && v.dataKey.type != null && v.dataKey.save) {
				needsSave = true;
				break;
			}
		}

		if (!needsSave) {
			return;
		}

		try {
			if (Files.notExists(path)) {
				Files.createDirectories(path.getParent());
			}

			var data = new CompoundTag();
			var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

			for (var v : map) {
				if (v.dataKey.type != null && v.dataKey.save && v.data != null && !v.data.equals(v.dataKey.defaultValue)) {
					try {
						data.put(v.dataKey.id, v.dataKey.type.codec().encodeStart(ops, Cast.to(v.data)).getOrThrow());
					} catch (Exception ex) {
						VidLib.LOGGER.error("Failed to save " + v.dataKey + ": " + ex);
					}
				}
			}

			try (var out = Files.newOutputStream(path)) {
				NbtIo.writeCompressed(data, out);
			}

			for (var v : map) {
				v.save = v.changeCount;
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to save data", ex);
		}
	}

	public void update(GlobalSessionData worldData, List<DataMapValue<K>> update) {
		for (var data : update) {
			if (data.key() != null) {
				init(data.key()).update(worldData, key, data.value());
			}
		}
	}

	public void syncAll(S2CPacketConsumer target, BiFunction<K, List<DataMapValue<K>>, SimplePayload> factory) {
		var list = new ArrayList<DataMapValue<K>>();

		for (var v : map) {
			if (v.dataKey.type != null && v.dataKey.sync) {
				list.add(new DataMapValue<>(v.dataKey, v.data));
			}
		}

		if (!list.isEmpty()) {
			target.s2c(factory.apply(key, list));
		}
	}

	public void sync(S2CPacketConsumer packetsToEveryone, BiFunction<K, List<DataMapValue<K>>, SimplePayload> factory) {
		List<DataMapValue<K>> syncAll = null;

		for (var v : map) {
			if (v.sync != v.changeCount && v.dataKey.type != null && v.dataKey.sync) {
				v.sync = v.changeCount;

				if (syncAll == null) {
					syncAll = new ArrayList<>();
				}

				syncAll.add(new DataMapValue<>(v.dataKey, v.data));
			}
		}

		if (syncAll != null) {
			packetsToEveryone.s2c(factory.apply(key, syncAll));
		}
	}
}
