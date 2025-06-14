package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.util.Cast;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.level.Level;

public interface SimplePacketPayload {
	VidLibPacketType<?> getType();

	default boolean allowDebugLogging() {
		return true;
	}

	default void handleAsync(Context ctx) {
		ctx.parent().enqueueWork(() -> handle(ctx));
	}

	default void handle(Context ctx) {
	}

	default ClientboundCustomPayloadPacket toS2C(Level level) {
		return new ClientboundCustomPayloadPacket(new VidLibPacketPayloadContainer(this, level.vl$nextPacketId(), level.getGameTime()));
	}

	default ServerboundCustomPayloadPacket toC2S(Level level) {
		return new ServerboundCustomPayloadPacket(new VidLibPacketPayloadContainer(this, level.vl$nextPacketId(), level.getGameTime()));
	}

	default byte[] toBytes(Level level, long uid) {
		var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
		buf.writeVarLong(uid);
		buf.writeVarLong(level.getGameTime());
		getType().streamCodec().encode(buf, Cast.to(this));
		var bytes = new byte[buf.readableBytes()];
		buf.getBytes(buf.readerIndex(), bytes);
		buf.release();
		return bytes;
	}
}
