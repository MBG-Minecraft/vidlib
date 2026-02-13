package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLGameTimeProvider;
import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class DataMap {
	public final UUID owner;
	public final DataKeyStorage storage;
	public final VLGameTimeProvider timeProvider;
	private final TrackedDataMapValue[] map;
	public DataMapOverrides.DataMap overrides;
	private Optional<Object>[] superOverrides;

	public DataMap(UUID owner, DataKeyStorage storage, VLGameTimeProvider timeProvider) {
		this.owner = owner;
		this.storage = storage;
		this.timeProvider = timeProvider;
		this.map = new TrackedDataMapValue[storage.all.size()];
		this.overrides = null;
		this.superOverrides = null;

		for (var type : storage.all.values()) {
			var value = new TrackedDataMapValue(type);
			value.data = type.defaultValue();
			this.map[type.index()] = value;
		}
	}

	TrackedDataMapValue init(DataKey<?> type) {
		return map[type.index()];
	}

	@SuppressWarnings("unchecked")
	public <T> T getActual(DataKey<T> type) {
		return (T) init(type).data;
	}

	public <T> T get(DataKey<T> type) {
		if (superOverrides != null) {
			var v = superOverrides[type.index()];

			if (v != null) {
				return Cast.to(v.orElse(null));
			}
		}

		if (overrides != null) {
			var v = overrides.getOverride(type, timeProvider.vl$getGameTime());

			if (v != null) {
				return v;
			}
		}

		return getActual(type);
	}

	public <T> void set(DataKey<T> type, @Nullable T value) {
		var v = init(type);
		v.data = value;
		v.setChanged();
	}

	public void setSuperOverride(DataKey<?> type, @Nullable Object value) {
		if (superOverrides == null) {
			superOverrides = new Optional[map.length];
		}

		superOverrides[type.index()] = Optional.ofNullable(value);
	}

	public void removeSuperOverride(DataKey<?> type) {
		if (superOverrides != null) {
			superOverrides[type.index()] = null;

			for (var value : superOverrides) {
				if (value != null) {
					return;
				}
			}

			superOverrides = null;
		}
	}

	public boolean hasSuperOverride(DataKey<?> type) {
		return superOverrides != null && superOverrides[type.index()] != null;
	}

	public <T> void reset(DataKey<T> type) {
		set(type, type.defaultValue());
	}

	public void load(MinecraftServer server, Path path) {
		for (var v : map) {
			v.data = v.key.defaultValue();
		}

		if (Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var data = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
				var ops = server.nbtOps();

				for (var type : storage.saved.values()) {
					var tag = data.get(type.id());

					if (tag != null) {
						var dataValue = type.type().codec().parse(ops, tag).getOrThrow();

						if (dataValue != null) {
							init(type).data = dataValue;
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
			if (v.save != v.changeCount && v.key.type() != null && v.key.save()) {
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
				if (v.key.type() != null && v.key.save() && v.data != null && !v.data.equals(v.key.defaultValue())) {
					data.put(v.key.id(), v.key.type().codec().encodeStart(ops, Cast.to(v.data)).getOrThrow());
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

	public void update(@Nullable Player player, List<DataMapValue> update) {
		for (var data : update) {
			if (data.key() != null) {
				init(data.key()).update(player, data.value());
			}
		}
	}

	public void syncAll(VLS2CPacketConsumer target, @Nullable Player selfPlayer, BiFunction<UUID, List<DataMapValue>, SimplePacketPayload> factory) {
		var list = new ArrayList<DataMapValue>();

		for (var v : map) {
			if (v.key.type() != null && v.key.sync()) {
				list.add(new DataMapValue(v.key, v.data));
			}
		}

		if (!list.isEmpty()) {
			target.s2c(factory.apply(owner, list));
		}
	}

	public void sync(VLS2CPacketConsumer packetsToEveryone, @Nullable Player selfPlayer, BiFunction<UUID, List<DataMapValue>, SimplePacketPayload> factory) {
		List<DataMapValue> syncAll = null;

		for (var v : map) {
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
