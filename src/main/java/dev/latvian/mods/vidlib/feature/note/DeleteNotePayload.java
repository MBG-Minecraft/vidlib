package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.UUID;

public record DeleteNotePayload(UUID id) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<DeleteNotePayload> TYPE = VidLibPacketType.internal("delete_note", KLibStreamCodecs.UUID.map(DeleteNotePayload::new, DeleteNotePayload::id));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().deleteNote(id);
	}
}
