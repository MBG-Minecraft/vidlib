package dev.latvian.mods.vidlib.feature.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface NetworkChannelInfo {
	boolean hasChannel(ResourceLocation payloadId);

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
