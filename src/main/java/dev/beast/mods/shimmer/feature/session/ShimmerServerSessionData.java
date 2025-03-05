package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

public class ShimmerServerSessionData extends ShimmerSessionData {
	public ShimmerServerSessionData(UUID uuid) {
		super(uuid);
	}

	@ApiStatus.Internal
	public void loadPlayerData(MinecraftServer server) {
		playerDataMap = null;

		var path = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("shimmer").resolve(uuid + ".nbt");

		if (Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var data = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
				var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

				for (var type : PlayerDataType.SAVED.values()) {
					var tag = data.get(type.id().toString());

					if (tag != null) {
						var playerData = type.codec().decode(ops, tag).getOrThrow().getFirst();

						if (playerData != null) {
							init(type).playerData = playerData;
						}
					}
				}
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Failed to load player data", ex);
			}
		}
	}

	@ApiStatus.Internal
	public void savePlayerData(MinecraftServer server) {
		boolean needsSave = false;

		if (playerDataMap != null) {
			for (var v : playerDataMap.values()) {
				if (v.save != v.playerData.changeCount && v.playerData.type().codec() != null) {
					needsSave = true;
					break;
				}
			}
		}

		if (!needsSave) {
			return;
		}

		var path = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("shimmer").resolve(uuid + ".nbt");

		try {
			if (Files.notExists(path)) {
				Files.createDirectories(path.getParent());
			}

			var data = new CompoundTag();
			var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

			for (var v : playerDataMap.values()) {
				if (v.playerData.type().codec() != null) {
					data.put(v.playerData.type().id().toString(), v.playerData.type().codec().encodeStart(ops, Cast.to(v.playerData)).getOrThrow());
				}
			}

			try (var out = Files.newOutputStream(path)) {
				NbtIo.writeCompressed(data, out);
			}

			for (var v : playerDataMap.values()) {
				v.save = v.playerData.changeCount;
			}
		} catch (Exception ex) {
			Shimmer.LOGGER.error("Failed to save player data", ex);
		}
	}

	@ApiStatus.Internal
	public void syncPlayerData(ServerPlayer player) {
		if (playerDataMap != null) {
			ArrayList<PlayerData> syncSelf = null, syncAll = null;

			for (var v : playerDataMap.values()) {
				if (v.sync != v.playerData.changeCount && v.playerData.type().streamCodec() != null) {
					v.sync = v.playerData.changeCount;

					if (v.playerData.type().syncToAllClients()) {
						if (syncAll == null) {
							syncAll = new ArrayList<>();
						}

						syncAll.add(v.playerData);
					} else {
						if (syncSelf == null) {
							syncSelf = new ArrayList<>();
						}

						syncSelf.add(v.playerData);
					}
				}
			}

			if (syncSelf != null) {
				player.s2c(new SyncPlayerDataPayload(player.getUUID(), syncSelf));
			}

			if (syncAll != null) {
				player.server.s2c(new SyncPlayerDataPayload(player.getUUID(), syncAll));
			}
		}
	}
}
