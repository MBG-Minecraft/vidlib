package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.feature.net.NetworkChannelInfo;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;

public record NeoForgeNetworkChannelInfo(ICommonPacketListener listener) implements NetworkChannelInfo {
	@Override
	public boolean hasChannel(Identifier payloadId) {
		return listener.hasChannel(payloadId);
	}
}
