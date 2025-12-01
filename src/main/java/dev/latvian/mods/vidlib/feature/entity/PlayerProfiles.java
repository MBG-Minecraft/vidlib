package dev.latvian.mods.vidlib.feature.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.util.UUIDTypeAdapter;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.MiscUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerProfiles {
	public static final Gson AUTH_GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

	public static final Map<UUID, PlayerProfile> BY_UUID = new ConcurrentHashMap<>();
	public static final Map<String, PlayerProfile> BY_NAME = new ConcurrentHashMap<>();
	private static List<PlayerProfile> allKnown = null;
	private static boolean shouldSave = false;

	static {
		load();
	}

	public static void load() {
		BY_UUID.clear();
		BY_NAME.clear();

		try {
			var path = VidLibPaths.USER.get().resolve("cached-profiles.json");

			if (Files.exists(path)) {
				try (var reader = Files.newBufferedReader(path)) {
					var json = JsonUtils.read(reader);
					var list = PlayerProfile.LIST_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

					for (var entry : list) {
						BY_UUID.put(entry.profile().getId(), entry);
						BY_NAME.put(entry.profile().getName().toLowerCase(Locale.ROOT), entry);
					}
				}
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load cached player profiles", ex);
		}
	}

	@AutoInit(AutoInit.Type.SAVE_GAME)
	public static void save() {
		if (!shouldSave) {
			return;
		}

		shouldSave = false;

		try (var writer = Files.newBufferedWriter(VidLibPaths.mkdirs(VidLibPaths.USER.get().resolve("cached-profiles.json")))) {
			var list = getAllKnown();
			var json = PlayerProfile.LIST_CODEC.encodeStart(JsonOps.INSTANCE, list).getOrThrow();
			JsonUtils.write(writer, json, false);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to save cached player profiles", ex);
		}
	}

	public static List<PlayerProfile> getAllKnown() {
		if (allKnown == null) {
			var map = new Object2ObjectOpenHashMap<UUID, PlayerProfile>(BY_UUID.size());

			for (var value : BY_UUID.values()) {
				if (!value.isError()) {
					map.put(value.profile().getId(), value);
				}
			}

			for (var value : BY_NAME.values()) {
				if (!value.isError()) {
					map.put(value.profile().getId(), value);
				}
			}

			var list = new ArrayList<>(map.values());
			list.sort((o1, o2) -> o1.profile().getName().compareToIgnoreCase(o2.profile().getName()));
			allKnown = list;
			return list;
		}

		return allKnown;
	}

	public static DataResult<UUID> fetchUUID(String input) {
		try {
			return DataResult.success(UndashedUuid.fromStringLenient(input));
		} catch (Exception ignore) {
		}

		if (input.length() < 3 || input.length() > 16) {
			return DataResult.error(() -> "Invalid name " + input + " size: " + input.length());
		} else if (!input.matches("\\w+")) {
			return DataResult.error(() -> "Invalid name " + input);
		}

		try {
			return MiscUtils.fetch("https://api.mojang.com/users/profiles/minecraft/" + input).flatMap(bytes -> {
				JsonObject json = null;

				try {
					json = JsonUtils.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8), JsonObject.class);
					var profile = ExtraCodecs.GAME_PROFILE.parse(JsonOps.INSTANCE, json);
					return profile.flatMap(p -> DataResult.success(p.getId()));
				} catch (Exception e) {
					var j = json;
					return DataResult.error(() -> "Failed to parse profile json " + j + ": " + e.getMessage());
				}
			});
		} catch (Exception ex) {
			return DataResult.error(() -> "Failed to fetch UUID from " + input + ": " + ex);
		}
	}

	public static DataResult<PlayerProfile> fetch(UUID uuid) {
		return MiscUtils.fetch("https://sessionserver.mojang.com/session/minecraft/profile/" + UndashedUuid.toString(uuid)).flatMap(bytes -> {
			JsonObject json = null;

			try {
				json = JsonUtils.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8), JsonObject.class);
				var profile = ExtraCodecs.GAME_PROFILE.parse(JsonOps.INSTANCE, json);
				return profile.flatMap(p -> DataResult.success(wrap(p)));
			} catch (Exception e) {
				var j = json;
				return DataResult.error(() -> "Failed to parse profile json " + j + ": " + e.getMessage());
			}
		});
	}

	public static PlayerProfile get(UUID uuid) {
		return BY_UUID.computeIfAbsent(uuid, id -> {
			for (var p : BY_NAME.values()) {
				if (!p.isError() && p.profile().getId().equals(id)) {
					return p;
				}
			}

			var profile = fetch(id).resultOrPartial().orElse(PlayerProfile.ERROR);
			shouldSave = true;
			allKnown = null;
			return profile;
		});
	}

	public static PlayerProfile get(String name) {
		BY_NAME.clear();

		return BY_NAME.computeIfAbsent(name.toLowerCase(Locale.ROOT), n -> {
			for (var p : BY_UUID.values()) {
				if (!p.isError() && p.profile().getName().equalsIgnoreCase(n)) {
					return p;
				}
			}

			var uuidResult = fetchUUID(n);

			if (uuidResult.isError()) {
				VidLib.LOGGER.error(uuidResult.error().map(DataResult.Error::message).orElse("Failed to fetch profile"));
				return PlayerProfile.ERROR;
			}

			var uuid = uuidResult.getOrThrow();
			VidLib.LOGGER.info("Fetched UUID of " + n + ": " + uuid);
			var profile = fetch(uuid).resultOrPartial().orElse(PlayerProfile.ERROR);
			shouldSave = true;
			allKnown = null;
			return profile;
		});
	}

	public static String getName(UUID uuid) {
		var p = get(uuid);
		return p.isError() ? UndashedUuid.toString(uuid) : p.profile().getName();
	}

	public static MinecraftProfileTextures unpackTextures(Property packedTextures) {
		var value = packedTextures.value();
		MinecraftTexturesPayload result;

		try {
			var json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
			result = AUTH_GSON.fromJson(json, MinecraftTexturesPayload.class);
		} catch (JsonParseException | IllegalArgumentException e) {
			VidLib.LOGGER.error("Could not decode textures payload", e);
			return MinecraftProfileTextures.EMPTY;
		}

		if (result == null || result.textures() == null || result.textures().isEmpty()) {
			return MinecraftProfileTextures.EMPTY;
		}

		var textures = result.textures();

		for (var entry : textures.entrySet()) {
			var url = entry.getValue().getUrl();

			if (url == null || !TextureUrlChecker.isAllowedTextureDomain(url)) {
				VidLib.LOGGER.error("Textures payload url is invalid: {}", url);
				return MinecraftProfileTextures.EMPTY;
			}
		}

		return new MinecraftProfileTextures(
			textures.get(MinecraftProfileTexture.Type.SKIN),
			textures.get(MinecraftProfileTexture.Type.CAPE),
			textures.get(MinecraftProfileTexture.Type.ELYTRA),
			SignatureState.SIGNED
		);
	}

	public static GameProfile withTextures(GameProfile profile) {
		return profile.getProperties().containsKey("textures") ? profile : get(profile.getId()).profile();
	}

	private static PlayerProfile wrap(GameProfile profile) {
		String skinUrl = "";
		boolean slimModel = false;

		try {
			for (var property : profile.getProperties().get("textures")) {
				if (property != null && !property.value().isEmpty()) {
					var textures = unpackTextures(property);

					if (textures.skin() != null) {
						skinUrl = textures.skin().getUrl();
						var meta = textures.skin().getMetadata("model");
						slimModel = meta != null && meta.equalsIgnoreCase("SLIM");
					}
				}
			}
		} catch (Exception ignored) {
		}

		return new PlayerProfile(profile, System.currentTimeMillis() / 1000L, Optional.ofNullable(skinUrl.isEmpty() ? null : skinUrl), slimModel);
	}

	public static void cache(GameProfile profile) {
		if (profile == null) {
			return;
		}

		var id = profile.getId();
		var name = profile.getName().toLowerCase(Locale.ROOT);

		if (!id.equals(PlayerProfile.EMPTY_GAME_PROFILE.getId()) && !name.isEmpty()) {
			if (!BY_UUID.containsKey(id) || !BY_NAME.containsKey(name)) {
				if (profile.getProperties().containsKey("textures")) {
					var p = wrap(profile);
					BY_UUID.put(id, p);
					BY_NAME.put(name, p);
					shouldSave = true;
					allKnown = null;
				} else {
					Util.backgroundExecutor().execute(() -> get(id));
				}
			}
		}
	}
}
