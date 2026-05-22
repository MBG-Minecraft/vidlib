package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;

public record PackSyncMeta(String id, String code, String version, String session) {
	public static final PackSyncMeta EMPTY = new PackSyncMeta("", "", "", "");

	public static final Codec<PackSyncMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(PackSyncMeta::id),
		Codec.STRING.optionalFieldOf("code", "").forGetter(PackSyncMeta::code),
		Codec.STRING.optionalFieldOf("version", "").forGetter(PackSyncMeta::version),
		Codec.STRING.optionalFieldOf("session", "").forGetter(PackSyncMeta::session)
	).apply(instance, (id, code, version, session) -> {
		var meta = new PackSyncMeta(id, code, version, session);
		return meta.isEmpty() ? EMPTY : meta;
	}));

	private static PackSyncMeta override = EMPTY;
	private static PackSyncMeta current = null;

	public static void setOverride(@Nullable PackSyncMeta meta) {
		override = meta == null || meta.isEmpty() ? EMPTY : meta;
	}

	public static PackSyncMeta getMeta() {
		if (override != EMPTY) {
			return override;
		}

		if (current == null) {
			current = EMPTY;
			var id = System.getProperty("dev.latvian.mods.packsync.id", "");

			if (!id.isEmpty()) {
				current = new PackSyncMeta(
					id,
					System.getProperty("dev.latvian.mods.packsync.code", ""),
					System.getProperty("dev.latvian.mods.packsync.version", ""),
					System.getProperty("dev.latvian.mods.packsync.session", "")
				);
			} else {
				try {
					var configPath = PlatformHelper.CURRENT.getModsDirectory().resolve("pack-sync.json");
					String code = "";

					if (Files.exists(configPath)) {
						String json = Files.readString(configPath);
						JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

						if (jsonObject.has("pack_id")) {
							id = jsonObject.get("pack_id").getAsString();
						}

						if (jsonObject.has("pack_code")) {
							code = jsonObject.get("pack_code").getAsString();
						}
					}

					if (!id.isEmpty() || !code.isEmpty()) {
						current = new PackSyncMeta(id, code, "", "");
					}
				} catch (Exception e) {
					VidLib.LOGGER.error("Failed to read pack-sync.json", e);
				}
			}
		}

		return current;
	}

	public boolean isEmpty() {
		return this == EMPTY || id.isEmpty() && code.isEmpty() && version.isEmpty() && session.isEmpty();
	}

	public String match() {
		return id.isEmpty() ? code : id;
	}

	public boolean matches(PackSyncMeta other) {
		if (isEmpty()) {
			return true;
		} else if (other.isEmpty()) {
			return false;
		} else {
			return match().equals(other.match());
		}
	}

	public PackSyncMeta editId(String id) {
		return new PackSyncMeta(id, code, version, session);
	}

	@Override
	@NotNull
	public String toString() {
		return match();
	}
}
