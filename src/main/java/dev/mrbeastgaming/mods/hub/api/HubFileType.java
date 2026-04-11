package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public record HubFileType(int type, String contentType) {
	public static final HubFileType UNKNOWN = new HubFileType(0, "");
	public static final HubFileType FLASHBACK_REPLAY_RECORDING = new HubFileType(1, "application/zip");
	public static final HubFileType FLASHBACK_REPLAY_EDITOR_STATE = new HubFileType(2, "application/json");
	public static final HubFileType VOICE_CHAT_RECORDING = new HubFileType(3, "audio/wav");

	public static HubFileType custom(String contentType) {
		return new HubFileType(0, contentType);
	}

	public static HubFileType fromId(int id) {
		return switch (id) {
			case 1 -> FLASHBACK_REPLAY_RECORDING;
			case 2 -> FLASHBACK_REPLAY_EDITOR_STATE;
			case 3 -> VOICE_CHAT_RECORDING;
			default -> new HubFileType(id, "");
		};
	}

	public static HubFileType fromJson(JsonElement json) {
		if (json instanceof JsonPrimitive p) {
			if (p.isNumber()) {
				return fromId(p.getAsInt());
			} else {
				return custom(p.getAsString());
			}
		} else {
			return UNKNOWN;
		}
	}

	public JsonElement toJson() {
		return type == 0 ? new JsonPrimitive(contentType) : new JsonPrimitive(type);
	}
}
