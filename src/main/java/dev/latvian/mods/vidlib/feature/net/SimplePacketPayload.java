package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.level.Level;

public interface SimplePacketPayload {
	VidLibPacketType<?> getType();

	default boolean allowDebugLogging() {
		return true;
	}

	default void handleAsync(Context ctx) {
		ctx.parent().enqueueWork(() -> {
			try {
				handle(ctx);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to handle packet '%s' #%,d @ %,d, %s".formatted(ctx.type(), ctx.uid(), ctx.remoteGameTime(), this), ex);
			}
		});
	}

	default void handle(Context ctx) {
	}

	default ClientboundCustomPayloadPacket toS2C(Level level, long uid) {
		return new ClientboundCustomPayloadPacket(new VidLibPacketPayloadContainer(this, uid, level.getGameTime()));
	}

	default ClientboundCustomPayloadPacket toS2C(Level level) {
		return toS2C(level, level.vl$nextPacketId());
	}

	default ServerboundCustomPayloadPacket toC2S(Level level, long uid) {
		return new ServerboundCustomPayloadPacket(new VidLibPacketPayloadContainer(this, uid, level.getGameTime()));
	}

	default ServerboundCustomPayloadPacket toC2S(Level level) {
		return toC2S(level, level.vl$nextPacketId());
	}

	default byte[] toBytes(Level level, long uid) {
		var buf = PlatformHelper.CURRENT.createBuffer(Unpooled.buffer(), level.registryAccess());
		var container = new VidLibPacketPayloadContainer(this, uid, level.getGameTime());
		getType().streamCodec().encode(buf, container);
		var bytes = new byte[buf.readableBytes()];
		buf.getBytes(buf.readerIndex(), bytes);
		buf.release();
		return bytes;
	}
}
