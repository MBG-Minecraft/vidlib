package dev.beast.mods.shimmer.util;

import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class S2CPacketBundleBuilder implements ShimmerS2CPacketConsumer {
	private List<Packet<? super ClientGamePacketListener>> list;

	@Override
	public void s2c(@Nullable Packet<? super ClientGamePacketListener> packet) {
		if (packet == null) {
			return;
		}

		if (list == null) {
			list = new ArrayList<>(1);
		}

		list.add(packet);
	}

	@Nullable
	public Packet<? super ClientGamePacketListener> createPacket() {
		if (list == null || list.isEmpty()) {
			return null;
		} else if (list.size() == 1) {
			return list.getFirst();
		} else {
			return new ClientboundBundlePacket(list);
		}
	}

	public void send(ShimmerS2CPacketConsumer other) {
		var packet = createPacket();

		if (packet != null) {
			other.s2c(packet);
		}
	}
}
