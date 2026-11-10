package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record CreateNotePayload(Note note) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<CreateNotePayload> TYPE = SimplePayloadType.vidlib("create_note", Note.STREAM_CODEC.map(CreateNotePayload::new, CreateNotePayload::note));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().createNote(note);
	}
}
