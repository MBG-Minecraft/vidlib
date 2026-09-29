package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import net.minecraft.network.PacketListener;

public interface VLClientConfigPacketListener extends VLClientCommonPacketListener {
	default void vl$transfer(VLClientPlayPacketListener play, PacketListener packetListener) {
		throw new NoMixinException(this);
	}
}
