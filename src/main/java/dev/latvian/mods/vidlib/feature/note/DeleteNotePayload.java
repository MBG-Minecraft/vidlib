package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.UUID;

public record DeleteNotePayload(UUID id) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<DeleteNotePayload> TYPE = SimplePayloadType.vidlib("delete_note", KLibStreamCodecs.UUID.map(DeleteNotePayload::new, DeleteNotePayload::id));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().deleteNote(id);
	}
}
