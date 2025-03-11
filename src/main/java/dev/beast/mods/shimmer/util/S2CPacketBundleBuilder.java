package dev.beast.mods.shimmer.util;

import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.ArrayList;
import java.util.List;

public class S2CPacketBundleBuilder implements ShimmerS2CPacketConsumer {
	private List<Packet<? super ClientGamePacketListener>> list;

	@Override
	public void s2c(Packet<? super ClientGamePacketListener> packet) {
		if (list == null) {
			list = new ArrayList<>(1);
		}

		list.add(packet);
	}

	public void send(ShimmerS2CPacketConsumer other) {
		if (list != null) {
			other.s2c(list);
		}
	}
}
