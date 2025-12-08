package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.atomic.AtomicLong;

public interface SimplePacketPayload {
	AtomicLong S2C = new AtomicLong(0L);
	AtomicLong C2S = new AtomicLong(0L);

	VidLibPacketType<?> getType();

	default boolean allowDebugLogging() {
		return true;
	}

	default void handleAsync(IPayloadContext payloadContext, long uid, long remoteGameTime) {
		payloadContext.enqueueWork(() -> {
			var ctx = new Context(payloadContext, getType().type().id(), uid, remoteGameTime);

			try {
				handle(ctx);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to handle packet '%s' #%,d @ %,d, %s".formatted(ctx.type(), ctx.uid(), ctx.remoteGameTime(), this), ex);
			}
		});
	}

	default void handle(Context ctx) {
	}

	default VidLibPacketPayloadContainer toS2C(long gameTime) {
		return new VidLibPacketPayloadContainer(this, S2C.incrementAndGet(), gameTime);
	}

	default VidLibPacketPayloadContainer toC2S(long gameTime) {
		return new VidLibPacketPayloadContainer(this, C2S.incrementAndGet(), gameTime);
	}

	default ClientboundCustomPayloadPacket toConfigS2C() {
		return toS2C(0L).toVanillaClientbound();
	}

	default ServerboundCustomPayloadPacket toConfigC2S() {
		return toC2S(0L).toVanillaServerbound();
	}

	default ClientboundCustomPayloadPacket toGameS2C(Level level) {
		return toS2C(level.getGameTime()).toVanillaClientbound();
	}

	default ServerboundCustomPayloadPacket toGameC2S(Level level) {
		return toC2S(level.getGameTime()).toVanillaServerbound();
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
