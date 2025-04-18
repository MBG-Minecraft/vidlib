package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.util.Cast;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class DataMap {
	public final UUID owner;
	private final DataTypeStorage storage;
	private Map<DataType<?>, TrackedDataMapValue> map;
	public DataRecorder.DataMap override;

	public DataMap(UUID owner, DataTypeStorage storage) {
		this.owner = owner;
		this.storage = storage;
	}

	TrackedDataMapValue init(DataType<?> type) {
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
	public <T> T get(DataType<T> type) {
		return (T) init(type).data;
	}

	public <T> T get(DataType<T> type, long gameTime) {
		if (override != null) {
			var v = override.getOverride(type, gameTime);

			if (v != null) {
				return v;
			}
		}

		return get(type);
	}

	public <T> void set(DataType<T> type, @Nullable T value) {
		var v = init(type);
		v.data = value;
		v.setChanged();
	}

	public void load(MinecraftServer server, Path path) {
		map = null;

		if (Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var data = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
				var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

				for (var type : storage.saved.values()) {
					var tag = data.get(type.id().toString());

					if (tag != null) {
						var playerData = type.type().codec().parse(ops, tag).getOrThrow();

						if (playerData != null) {
							init(type).data = playerData;
						}
					}
				}
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Failed to load data", ex);
			}
		}
	}

	public void save(MinecraftServer server, Path path) {
		boolean needsSave = false;

		if (map != null) {
			for (var v : map.values()) {
				if (v.save != v.changeCount && v.type.type() != null && v.type.save()) {
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
				if (v.type.type() != null && v.type.save()) {
					data.put(v.type.id().toString(), v.type.type().codec().encodeStart(ops, Cast.to(v.data)).getOrThrow());
				}
			}

			try (var out = Files.newOutputStream(path)) {
				NbtIo.writeCompressed(data, out);
			}

			for (var v : map.values()) {
				v.save = v.changeCount;
			}
		} catch (Exception ex) {
			Shimmer.LOGGER.error("Failed to save data", ex);
		}
	}

	public void update(@Nullable Player player, List<DataMapValue> update) {
		for (var data : update) {
			init(data.type()).update(player, data.value());
		}
	}

	public void syncAll(ShimmerS2CPacketConsumer target, @Nullable ServerPlayer selfPlayer, BiFunction<UUID, List<DataMapValue>, ShimmerPacketPayload> factory) {
		var list = new ArrayList<DataMapValue>();

		if (map != null) {
			for (var v : map.values()) {
				if (v.type.type() != null && v.type.sync() && (v.type.syncToAllClients() || selfPlayer != null && owner.equals(selfPlayer.getUUID()))) {
					list.add(new DataMapValue(v.type, v.data));
				}
			}
		}

		if (!list.isEmpty()) {
			target.s2c(factory.apply(owner, list));
		}
	}

	public void sync(ShimmerS2CPacketConsumer packetsToEveryone, @Nullable ServerPlayer selfPlayer, BiFunction<UUID, List<DataMapValue>, ShimmerPacketPayload> factory) {
		if (map == null) {
			return;
		}

		List<DataMapValue> syncSelf = null, syncAll = null;

		for (var v : map.values()) {
			if (v.sync != v.changeCount && v.type.type() != null && v.type.sync()) {
				v.sync = v.changeCount;

				if (v.type.syncToAllClients()) {
					if (syncAll == null) {
						syncAll = new ArrayList<>();
					}

					syncAll.add(new DataMapValue(v.type, v.data));
				} else if (selfPlayer != null) {
					if (syncSelf == null) {
						syncSelf = new ArrayList<>();
					}

					syncSelf.add(new DataMapValue(v.type, v.data));
				}
			}
		}

		if (syncSelf != null) {
			selfPlayer.s2c(factory.apply(selfPlayer.getUUID(), syncSelf));
		}

		if (syncAll != null) {
			packetsToEveryone.s2c(factory.apply(owner, syncAll));
		}
	}
}
