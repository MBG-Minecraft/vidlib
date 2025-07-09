package dev.latvian.mods.vidlib.feature.file;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FileChunkPayload(int id, int partIndex, byte[] chunk) implements SimplePacketPayload {

	public static final StreamCodec<RegistryFriendlyByteBuf, FileChunkPayload> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FileChunkPayload::id,
		ByteBufCodecs.VAR_INT, FileChunkPayload::partIndex,
		ByteBufCodecs.BYTE_ARRAY, FileChunkPayload::chunk, FileChunkPayload::new
	);

	@AutoPacket
	public static final VidLibPacketType<FileChunkPayload> TYPE =
		VidLibPacketType.internal("file_chunk", FileChunkPayload.STREAM_CODEC);

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
		ReceivingFileTransferManager.addChunk(id, partIndex, chunk);
	}

}
