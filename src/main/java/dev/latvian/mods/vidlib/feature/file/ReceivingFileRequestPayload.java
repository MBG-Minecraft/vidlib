package dev.latvian.mods.vidlib.feature.file;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;

public record ReceivingFileRequestPayload(int id, long snowflake) implements SimplePacketPayload {

	@AutoPacket
	public static final VidLibPacketType<ReceivingFileRequestPayload> TYPE =
		VidLibPacketType.internal("file_request_id", CompositeStreamCodec.of(
			ByteBufCodecs.VAR_INT, ReceivingFileRequestPayload::id,
			ByteBufCodecs.VAR_LONG, ReceivingFileRequestPayload::snowflake,
			ReceivingFileRequestPayload::new
		));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		return false;
	}

	@Override
	public void handle(Context ctx) {
		SendingFileTransferManager.responseToRequest(id, snowflake);
	}
}
