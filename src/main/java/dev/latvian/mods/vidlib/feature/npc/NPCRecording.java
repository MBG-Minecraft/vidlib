package dev.latvian.mods.vidlib.feature.npc;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.util.Map;

public class NPCRecording {
	public static Object2ObjectSortedMap<String, Lazy<NPCRecording>> REPLAY = null;

	public static Object2ObjectSortedMap<String, Lazy<NPCRecording>> getReplay(RegistryAccess registryAccess) {
		if (REPLAY == null) {
			REPLAY = new Object2ObjectLinkedOpenHashMap<>();

			var rootPath = VidLibPaths.GAME.resolve("npc");

			if (Files.exists(rootPath)) {
				try (var stream = Files.walk(rootPath)) {
					for (var path : stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".npcrec")).sorted().toList()) {
						var fname = rootPath.relativize(path).toString();
						var name = fname.substring(0, fname.length() - 7);

						REPLAY.put(name, Lazy.of(() -> {
							try (var in = new BufferedInputStream(Files.newInputStream(path))) {
								var buf = PlatformHelper.CURRENT.createBuffer(Unpooled.wrappedBuffer(in.readAllBytes()), registryAccess);
								var recording = new NPCRecording(buf);
								VidLib.LOGGER.info("Loaded NPC recording '" + name + "'");
								return recording;
							} catch (Exception ex) {
								VidLib.LOGGER.error("Failed to load NPC recording '" + name + "'", ex);
								return null;
							}
						}));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (!REPLAY.isEmpty()) {
				REPLAY.put("latest", REPLAY.lastEntry().getValue());
			}
		}

		return REPLAY;
	}

	public final long start;
	public final GameProfile profile;
	public final Map<NPCDataType<?>, NPCTypeRecording<?>> actions;
	public long length;

	public NPCRecording(GameProfile profile) {
		this.start = System.currentTimeMillis();
		this.profile = profile;
		this.actions = new Reference2ObjectLinkedOpenHashMap<>(NPCDataType.MAP.size());

		for (var type : NPCDataType.MAP.values()) {
			actions.put(type, new NPCTypeRecording(type));
		}
	}

	public NPCRecording(RegistryFriendlyByteBuf buf) {
		this(ByteBufCodecs.GAME_PROFILE.decode(buf));
		this.length = VarLong.read(buf);
		int size = VarInt.read(buf);

		for (int i = 0; i < size; i++) {
			var type = actions.get(NPCDataType.MAP.get(ByteBufCodecs.STRING_UTF8.decode(buf)));
			type.read(buf);
		}
	}

	public void record(long now, float delta, Player player) {
		length = now - start;

		for (var action : actions.values()) {
			action.record(length, delta, player);
		}
	}

	public void write(RegistryFriendlyByteBuf buf) {
		ByteBufCodecs.GAME_PROFILE.encode(buf, profile);
		VarLong.write(buf, length);
		VarInt.write(buf, actions.size());

		for (var rec : actions.values()) {
			ByteBufCodecs.STRING_UTF8.encode(buf, rec.type.name());
			rec.write(buf);
		}
	}

	@Override
	public String toString() {
		return "NPCRecording[profile=" + profile + ", actions=" + actions + "]";
	}

	public <T> T get(NPCDataType<T> type, long offset) {
		var map = actions.get(type);
		return map == null ? type.defaultValue() : (T) map.get(offset);
	}
}
