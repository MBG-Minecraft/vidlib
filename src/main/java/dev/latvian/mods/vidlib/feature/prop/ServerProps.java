package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class ServerProps extends Props<ServerLevel> {
	public ServerProps(ServerLevel level) {
		super(level);
	}

	@Override
	public void tick(boolean tick) {
		var updates = tick ? new S2CPacketBundleBuilder() : null;

		for (var list : propLists.values()) {
			list.tick(updates, tick);
		}

		if (updates != null) {
			updates.send(level);
		}
	}

	@Override
	public void tickBukkit(@Nullable S2CPacketConsumer updates, boolean tick) {
		for (var list : propLists.values()) {
			list.tickBukkit(updates, tick);
		}
	}
}
