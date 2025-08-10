package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class DataMap {
	public final UUID owner;
	private final DataKeyStorage storage;
	private Map<DataKey<?>, TrackedDataMapValue> map;
	public DataMapOverrides.DataMap overrides;
	public Map<DataKey<?>, Optional<Object>> superOverrides;

	public DataMap(UUID owner, DataKeyStorage storage) {
		this.owner = owner;
		this.storage = storage;
	}

	TrackedDataMapValue init(DataKey<?> type) {
		if (map == null) {
			map = new Reference2ObjectOpenHashMap<>();
		}

		var value = map.get(type);

		if (value == null) {
			value = new TrackedDataMapValue(type);
			value.data = type.defaultValue();
			map.put(type, value);
		}

		return value;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(DataKey<T> type) {
		return (T) init(type).data;
	}

	public <T> T get(DataKey<T> type, long gameTime) {
		if (superOverrides != null) {
			var v = superOverrides.get(type);

			if (v != null) {
				return Cast.to(v.orElse(null));
			}
		}

		if (overrides != null) {
			var v = overrides.getOverride(type, gameTime);

			if (v != null) {
				return v;
			}
		}

		return get(type);
	}

	public <T> void set(DataKey<T> type, @Nullable T value) {
		var v = init(type);
		v.data = value;
		v.setChanged();
	}

	public void setSuperOverride(DataKey<?> type, @Nullable Object value) {
		if (superOverrides == null) {
			superOverrides = new Reference2ObjectArrayMap<>();
		}

		superOverrides.put(type, Optional.ofNullable(value));
	}

	public void removeSuperOverride(DataKey<?> type) {
		if (superOverrides != null) {
			superOverrides.remove(type);

			if (superOverrides.isEmpty()) {
				superOverrides = null;
			}
		}
	}

	public <T> void reset(DataKey<T> type) {
		set(type, type.defaultValue());
	}

	public void load(MinecraftServer server, Path path) {
		map = null;

		if (Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var data = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
				var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

				for (var type : storage.saved.values()) {
					var tag = data.get(type.id());

					if (tag != null) {
						var playerData = type.type().codec().parse(ops, tag).getOrThrow();

						if (playerData != null) {
							init(type).data = playerData;
						}
					}
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to load data", ex);
			}
		}
	}

	public void save(MinecraftServer server, Path path) {
		boolean needsSave = false;

		if (map != null) {
			for (var v : map.values()) {
				if (v.save != v.changeCount && v.key.type() != null && v.key.save()) {
					needsSave = true;
					break;
				}
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

			for (var v : map.values()) {
				if (v.key.type() != null && v.key.save() && v.data != null && !v.data.equals(v.key.defaultValue())) {
					data.put(v.key.id(), v.key.type().codec().encodeStart(ops, Cast.to(v.data)).getOrThrow());
				}
			}

			try (var out = Files.newOutputStream(path)) {
				NbtIo.writeCompressed(data, out);
			}

			for (var v : map.values()) {
				v.save = v.changeCount;
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to save data", ex);
		}
	}

	public void update(@Nullable Player player, List<DataMapValue> update) {
		for (var data : update) {
			if (data.key() != null) {
				init(data.key()).update(player, data.value());
			}
		}
	}

	public void syncAll(VLS2CPacketConsumer target, @Nullable Player selfPlayer, BiFunction<UUID, List<DataMapValue>, SimplePacketPayload> factory) {
		var list = new ArrayList<DataMapValue>();

		if (map != null) {
			for (var v : map.values()) {
				if (v.key.type() != null && v.key.sync()) {
					list.add(new DataMapValue(v.key, v.data));
				}
			}
		}

		if (!list.isEmpty()) {
			target.s2c(factory.apply(owner, list));
		}
	}

	public void sync(VLS2CPacketConsumer packetsToEveryone, @Nullable Player selfPlayer, BiFunction<UUID, List<DataMapValue>, SimplePacketPayload> factory) {
		if (map == null) {
			return;
		}

		List<DataMapValue> syncAll = null;

		for (var v : map.values()) {
			if (v.sync != v.changeCount && v.key.type() != null && v.key.sync()) {
				v.sync = v.changeCount;

				if (syncAll == null) {
					syncAll = new ArrayList<>();
				}

				syncAll.add(new DataMapValue(v.key, v.data));
			}
		}

		if (syncAll != null) {
			packetsToEveryone.s2c(factory.apply(owner, syncAll));
		}
	}
}
