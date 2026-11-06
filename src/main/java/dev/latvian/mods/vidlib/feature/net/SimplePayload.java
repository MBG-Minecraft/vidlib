package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;

public interface SimplePayload extends CustomPacketPayload {
	SimplePayloadType<?> getType();

	@Override
	default Type<? extends CustomPacketPayload> type() {
		return getType().type();
	}

	default boolean allowDebugLogging() {
		return true;
	}

	default void handleAsync(Context ctx) {
		ctx.enqueueWork(() -> {
			try {
				handle(ctx);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to handle packet '%s' @ %s".formatted(getType().type().id(), this), ex);
			}
		});
	}

	default void handle(Context ctx) {
	}

	default ClientboundCustomPayloadPacket toS2CPacket() {
		return new ClientboundCustomPayloadPacket(this);
	}

	default ServerboundCustomPayloadPacket toC2SPacket() {
		return new ServerboundCustomPayloadPacket(this);
	}

	default byte[] toBytes(Level level) {
		var buf = PlatformHelper.INSTANCE.createBuffer(Unpooled.buffer(), level.registryAccess());
		getType().streamCodec().encode(buf, Cast.to(this));
		return IOUtils.toByteArray(buf, true);
	}
}
