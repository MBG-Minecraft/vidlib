package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class DataMap {
	public final UUID owner;
	private final DataTypeStorage storage;
	private Map<DataType<?>, TrackedDataMapValue> map;

	public DataMap(UUID owner, DataTypeStorage storage) {
		this.owner = owner;
		this.storage = storage;
	}

	TrackedDataMapValue init(DataType<?> type) {
		if (map == null) {
			map = new IdentityHashMap<>();
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

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getOrNull(DataType<T> type) {
		if (map == null) {
			return null;
		}

		var value = map.get(type);

		if (value == null) {
			return null;
		}

		return (T) value.data;
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
						var playerData = type.codec().parse(ops, tag).getOrThrow();

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
				if (v.save != v.changeCount && v.type.codec() != null) {
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
				if (v.type.codec() != null) {
					data.put(v.type.id().toString(), v.type.codec().encodeStart(ops, Cast.to(v.data)).getOrThrow());
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
			var v = init(data.type());
			v.data = data.value();

			if (player != null && data.type().onReceived() != null) {
				data.type().onReceived().accept(player);
			}
		}
	}

	public void syncAll(ShimmerS2CPacketConsumer target, @Nullable ServerPlayer selfPlayer, BiFunction<UUID, List<DataMapValue>, CustomPacketPayload> factory) {
		var list = new ArrayList<DataMapValue>();

		if (map != null) {
			for (var v : map.values()) {
				if (v.type.streamCodec() != null && (v.type.syncToAllClients() || selfPlayer != null && owner.equals(selfPlayer.getUUID()))) {
					list.add(new DataMapValue(v.type, v.data));
				}
			}
		}

		if (!list.isEmpty()) {
			target.s2c(factory.apply(owner, list));
		}
	}

	public void sync(ShimmerS2CPacketConsumer packetsToEveryone, @Nullable ServerPlayer selfPlayer, BiFunction<UUID, List<DataMapValue>, CustomPacketPayload> factory) {
		if (map == null) {
			return;
		}

		List<DataMapValue> syncSelf = null, syncAll = null;

		for (var v : map.values()) {
			if (v.sync != v.changeCount && v.type.streamCodec() != null) {
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
