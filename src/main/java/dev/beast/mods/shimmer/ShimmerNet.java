package dev.beast.mods.shimmer;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ShimmerNet {
	static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String id) {
		return new CustomPacketPayload.Type<>(Shimmer.id(id));
	}
}
