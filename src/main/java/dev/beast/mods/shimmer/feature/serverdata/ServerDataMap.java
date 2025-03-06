package dev.beast.mods.shimmer.feature.serverdata;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerEntityContainer;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ServerDataMap {
	Map<ServerDataType<?>, ServerDataMapValue> serverDataMap;

	private ServerDataMapValue init(ServerDataType<?> type) {
		if (serverDataMap == null) {
			serverDataMap = new IdentityHashMap<>(1);
		}

		var v = serverDataMap.get(type);

		if (v == null) {
			v = new ServerDataMapValue();
			v.serverData = type.factory().get();
			serverDataMap.put(type, v);
		}

		return v;
	}

	@SuppressWarnings("unchecked")
	public <T extends ServerData> T get(ServerDataType<T> type) {
		return (T) init(type).serverData;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ServerData> T getOrNull(ServerDataType<T> type) {
		if (serverDataMap == null) {
			return null;
		}

		var v = serverDataMap.get(type);

		if (v == null) {
			return null;
		}

		return (T) v.serverData;
	}

	public void tick(MinecraftServer server) {
		if (serverDataMap == null) {
			return;
		}

		var updates = new ArrayList<ServerData>();

		for (var v : serverDataMap.values()) {
			if (v.sync != v.serverData.changeCount && v.serverData.type().streamCodec() != null) {
				v.sync = v.serverData.changeCount;
				updates.add(v.serverData);
			}
		}

		if (!updates.isEmpty()) {
			server.s2c(new SyncServerDataPayload(updates));
		}
	}

	public void load(MinecraftServer server, Path path) {
		serverDataMap = null;

		if (Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var data = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
				var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

				for (var type : ServerDataType.SAVED.values()) {
					var tag = data.get(type.id().toString());

					if (tag != null) {
						var playerData = type.codec().decode(ops, tag).getOrThrow().getFirst();

						if (playerData != null) {
							init(type).serverData = playerData;
						}
					}
				}
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Failed to load server data", ex);
			}
		}
	}

	public void save(MinecraftServer server, Path path) {
		boolean needsSave = false;

		if (serverDataMap != null) {
			for (var v : serverDataMap.values()) {
				if (v.save != v.serverData.changeCount && v.serverData.type().codec() != null) {
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

			for (var v : serverDataMap.values()) {
				if (v.serverData.type().codec() != null) {
					data.put(v.serverData.type().id().toString(), v.serverData.type().codec().encodeStart(ops, Cast.to(v.serverData)).getOrThrow());
				}
			}

			try (var out = Files.newOutputStream(path)) {
				NbtIo.writeCompressed(data, out);
			}

			for (var v : serverDataMap.values()) {
				v.save = v.serverData.changeCount;
			}
		} catch (Exception ex) {
			Shimmer.LOGGER.error("Failed to save server data", ex);
		}
	}

	public void syncAll(ShimmerEntityContainer target) {
		var list = new ArrayList<ServerData>();

		if (serverDataMap != null) {
			for (var v : serverDataMap.values()) {
				if (v.serverData.type().streamCodec() != null) {
					list.add(v.serverData);
				}
			}
		}

		target.s2c(new SyncServerDataPayload(list));
	}

	public void update(List<ServerData> update) {
		for (var data : update) {
			init(data.type()).serverData = data;
		}
	}
}
