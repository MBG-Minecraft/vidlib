package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class S2CPacketBundleBuilder implements VLS2CPacketConsumer {
	private static final int MAX_PER_BUNDLE = 4095;

	public final Level level;
	private List<Packet<? super ClientGamePacketListener>> list;

	public S2CPacketBundleBuilder(Level level) {
		this.level = level;
	}

	@Override
	public Level vl$level() {
		return level;
	}

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

	public void send(VLS2CPacketConsumer other) {
		var list1 = list;

		if (list1 == null) {
			return;
		}

		while (!list1.isEmpty()) {
			if (list1.size() == 1) {
				other.s2c(list1.getFirst());
				return;
			} else if (list1.size() <= MAX_PER_BUNDLE) {
				other.s2c(new ClientboundBundlePacket(list1));
				return;
			} else {
				var packet = new ClientboundBundlePacket(list1.subList(0, MAX_PER_BUNDLE));
				other.s2c(packet);
				list1 = list1.subList(MAX_PER_BUNDLE, list1.size());
			}
		}
	}

	public void sendUnbundled(VLS2CPacketConsumer other) {
		if (list == null || list.isEmpty()) {
			return;
		}

		for (var packet : list) {
			sendUnbundled0(other, packet);
		}
	}

	private void sendUnbundled0(VLS2CPacketConsumer other, Packet<? super ClientGamePacketListener> packet) {
		if (packet instanceof ClientboundBundlePacket bundle) {
			for (var packet1 : bundle.subPackets()) {
				sendUnbundled0(other, packet1);
			}
		} else {
			other.s2c(packet);
		}
	}
}
