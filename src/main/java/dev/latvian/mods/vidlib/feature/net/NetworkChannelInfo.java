package dev.latvian.mods.vidlib.feature.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public interface NetworkChannelInfo {
	boolean hasChannel(Identifier payloadId);

	default boolean hasChannel(CustomPacketPayload.Type<?> type) {
		return hasChannel(type.id());
	}

	default boolean hasChannel(CustomPacketPayload payload) {
		return hasChannel(payload.type());
	}

	default boolean hasChannel(VidLibPacketType<?> type) {
		return hasChannel(type.type());
	}
}
