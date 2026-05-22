package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record CreateNotePayload(Note note) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<CreateNotePayload> TYPE = VidLibPacketType.internal("create_note", Note.STREAM_CODEC.map(CreateNotePayload::new, CreateNotePayload::note));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().createNote(note);
	}
}
