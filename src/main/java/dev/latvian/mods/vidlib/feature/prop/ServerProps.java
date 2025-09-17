package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import net.minecraft.server.level.ServerLevel;

public class ServerProps extends Props<ServerLevel> {
	public ServerProps(ServerLevel level) {
		super(level);
	}

	@Override
	public void tick(boolean tick) {
		var updates = tick ? new S2CPacketBundleBuilder(level) : null;

		for (var list : propLists.values()) {
			list.tick(updates, tick);
		}

		if (updates != null) {
			updates.send(level);
		}
	}
}
