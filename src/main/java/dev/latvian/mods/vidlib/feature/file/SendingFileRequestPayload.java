package dev.latvian.mods.vidlib.feature.file;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;

/**
 * This packet is sent to request a file transfer.
 * It includes the total number of parts expected, a unique identifier (snowflake),
 * and the desired path on the other end.
 * <p>
 * The snowflake is used to correlate the request with the response.
 */
public record SendingFileRequestPayload(int totalParts, long snowflake, String path) implements SimplePacketPayload {

	@AutoPacket
	public static final VidLibPacketType<SendingFileRequestPayload> TYPE =
		VidLibPacketType.internal("file_request", CompositeStreamCodec.of(
			ByteBufCodecs.VAR_INT, SendingFileRequestPayload::totalParts,
			ByteBufCodecs.VAR_LONG, SendingFileRequestPayload::snowflake,
			ByteBufCodecs.STRING_UTF8, SendingFileRequestPayload::path,
			SendingFileRequestPayload::new
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
		ReceivingFileRequestPayload packet = new ReceivingFileRequestPayload(ReceivingFileTransferManager.getRandomId(totalParts, path), snowflake);
		Level level = ctx.level();
		if (level.isClientSide()) {
			level.c2s(packet.toC2S(level));
		} else {
			level.s2c(packet.toS2C(level));
		}
	}
}
