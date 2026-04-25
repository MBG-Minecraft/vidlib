package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.mods.klib.io.FileInfo;
import dev.mrbeastgaming.mods.hub.file.FileTypeProvider;

public record HubFileType(int type, String contentType) implements FileTypeProvider {
	public static final HubFileType UNKNOWN = new HubFileType(0, "");
	public static final HubFileType FLASHBACK_REPLAY_RECORDING = new HubFileType(1, MimeType.ZIP);
	public static final HubFileType FLASHBACK_REPLAY_EDITOR_STATE = new HubFileType(2, MimeType.JSON);
	public static final HubFileType VOICE_CHAT_RECORDING = new HubFileType(3, MimeType.MP3);
	public static final HubFileType CLIENT_CRASH_REPORT = new HubFileType(4, MimeType.TEXT);
	public static final HubFileType JVM_CRASH_REPORT = new HubFileType(5, MimeType.TEXT);
	public static final HubFileType GAME_LOG = new HubFileType(6, MimeType.TEXT);
	public static final HubFileType SERVER_CRASH_REPORT = new HubFileType(7, MimeType.TEXT);

	public static HubFileType custom(String contentType) {
		return new HubFileType(0, contentType);
	}

	public JsonElement toJson() {
		return type == 0 ? new JsonPrimitive(contentType) : new JsonPrimitive(type);
	}

	@Override
	public HubFileType getFileType(FileInfo fileInfo) {
		return this;
	}
}
