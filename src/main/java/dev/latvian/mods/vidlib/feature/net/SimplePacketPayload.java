package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.util.IOUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicLong;

public interface SimplePacketPayload {
	AtomicLong S2C = new AtomicLong(0L);
	AtomicLong C2S = new AtomicLong(0L);

	VidLibPacketType<?> getType();

	default boolean allowDebugLogging() {
		return true;
	}

	default void handleAsync(Context ctx) {
		ctx.enqueueWork(() -> {
			try {
				handle(ctx);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to handle packet '%s' #%,d @ %,d, %s".formatted(getType().type().id(), ctx.uid(), ctx.remoteGameTime(), this), ex);
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
		return new ClientboundCustomPayloadPacket(toS2C(0L));
	}

	default ServerboundCustomPayloadPacket toConfigC2S() {
		return new ServerboundCustomPayloadPacket(toC2S(0L));
	}

	default ClientboundCustomPayloadPacket toGameS2C(Level level) {
		return new ClientboundCustomPayloadPacket(toS2C(level.getGameTime()));
	}

	default ServerboundCustomPayloadPacket toGameC2S(Level level) {
		return new ServerboundCustomPayloadPacket(toC2S(level.getGameTime()));
	}

	default byte[] toBytes(Level level, long uid) {
		var buf = PlatformHelper.CURRENT.createBuffer(Unpooled.buffer(), level.registryAccess());
		var container = new VidLibPacketPayloadContainer(this, uid, level.getGameTime());
		getType().streamCodec().encode(buf, container);
		return IOUtils.toByteArray(buf, true);
	}
}
