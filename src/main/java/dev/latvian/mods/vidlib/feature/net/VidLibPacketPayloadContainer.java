package dev.latvian.mods.vidlib.feature.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VidLibPacketPayloadContainer(SimplePacketPayload wrapped, long uid, long remoteGameTime) implements CustomPacketPayload {
	@Override
	public Type<VidLibPacketPayloadContainer> type() {
		return wrapped.getType().type();
	}
}
