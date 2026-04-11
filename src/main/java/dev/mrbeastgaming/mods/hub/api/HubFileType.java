package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.mrbeastgaming.mods.hub.FileTypeProvider;

import java.nio.file.Path;

public record HubFileType(int type, String contentType) implements FileTypeProvider {
	public static final HubFileType UNKNOWN = new HubFileType(0, "");
	public static final HubFileType FLASHBACK_REPLAY_RECORDING = new HubFileType(1, "application/zip");
	public static final HubFileType FLASHBACK_REPLAY_EDITOR_STATE = new HubFileType(2, "application/json");
	public static final HubFileType VOICE_CHAT_RECORDING = new HubFileType(3, "audio/wav");
	public static final HubFileType CRASH_REPORT = new HubFileType(4, "text/plain");
	public static final HubFileType JVM_CRASH_REPORT = new HubFileType(5, "text/plain");
	public static final HubFileType GAME_LOG = new HubFileType(6, "text/plain");

	public static HubFileType custom(String contentType) {
		return new HubFileType(0, contentType);
	}

	public JsonElement toJson() {
		return type == 0 ? new JsonPrimitive(contentType) : new JsonPrimitive(type);
	}

	@Override
	public HubFileType getFileType(Path path) {
		return this;
	}
}
