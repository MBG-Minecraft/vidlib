package dev.mrbeastgaming.mods.hub.api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.apps.tinyhttp.content.MimeType;
import dev.mrbeastgaming.mods.hub.file.HubUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubUploadBuilderCallback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record HubFileType(int type, String contentType, String name) implements HubUploadBuilderCallback {
	public static final HubFileType UNKNOWN = new HubFileType(0, "", "");
	public static final HubFileType FLASHBACK_REPLAY_RECORDING = new HubFileType(1, MimeType.ZIP, "Flashback Replay Recordings");
	public static final HubFileType FLASHBACK_REPLAY_EDITOR_STATE = new HubFileType(2, MimeType.JSON, "Flashback Replay Editor States");
	public static final HubFileType VOICE_CHAT_RECORDING = new HubFileType(3, MimeType.MP3, "Voice Chat Recordings");
	public static final HubFileType CLIENT_CRASH_REPORT = new HubFileType(4, MimeType.TEXT, "Client Game Crash Reports");
	public static final HubFileType CLIENT_JVM_CRASH_REPORT = new HubFileType(5, MimeType.TEXT, "Client JVM Crash Reports");
	public static final HubFileType CLIENT_GAME_LOG = new HubFileType(6, MimeType.TEXT, "Client Game Logs");
	public static final HubFileType SERVER_CRASH_REPORT = new HubFileType(7, MimeType.TEXT, "Server Game Crash Reports");
	public static final HubFileType SERVER_JVM_CRASH_REPORT = new HubFileType(8, MimeType.TEXT, "Server JVM Crash Reports");
	public static final HubFileType SERVER_GAME_LOG = new HubFileType(9, MimeType.TEXT, "Server Game Logs");
	public static final HubFileType IMAGE = new HubFileType(10, MimeType.PNG, "Images");
	public static final HubFileType DEBUG = new HubFileType(11, "", "Debug");
	public static final HubFileType DIRECTORY = new HubFileType(12, "", "Directory");
	public static final HubFileType MOD_LOADER_CRASH_REPORT = new HubFileType(13, MimeType.TEXT, "Mod Loader Crash Reports");

	public static final List<HubFileType> TYPES = List.of(
		DEBUG,
		FLASHBACK_REPLAY_RECORDING,
		FLASHBACK_REPLAY_EDITOR_STATE,
		VOICE_CHAT_RECORDING,
		CLIENT_CRASH_REPORT,
		CLIENT_JVM_CRASH_REPORT,
		CLIENT_GAME_LOG,
		SERVER_CRASH_REPORT,
		SERVER_JVM_CRASH_REPORT,
		SERVER_GAME_LOG,
		IMAGE,
		DIRECTORY,
		MOD_LOADER_CRASH_REPORT
	);

	public static HubFileType custom(String contentType) {
		return new HubFileType(0, contentType, "");
	}

	public static HubFileType of(int type) {
		for (var t : TYPES) {
			if (t.type == type) {
				return t;
			}
		}

		return new HubFileType(type, "", "");
	}

	public static final Codec<HubFileType> CODEC = Codec.either(Codec.INT, Codec.STRING).xmap(either -> either.map(HubFileType::of, HubFileType::custom), type -> type.type == 0 ? Either.right(type.contentType) : Either.left(type.type));

	public static HubFileType probe(Path path) {
		try {
			var type = Files.probeContentType(path);
			return type == null || type.isEmpty() ? HubFileType.UNKNOWN : HubFileType.custom(type);
		} catch (Exception ignored) {
			return HubFileType.UNKNOWN;
		}
	}

	public JsonElement toJson() {
		return type == 0 ? new JsonPrimitive(contentType) : new JsonPrimitive(type);
	}

	@Override
	public void build(HubUploadBuilder builder) {
		builder.setType(this);
	}
}
