package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import net.minecraft.server.level.ServerLevel;

public class ServerProps extends Props<ServerLevel> {
	public ServerProps(ServerLevel level) {
		super(level);
	}

	@Override
	public void tick() {
		var updates = new S2CPacketBundleBuilder(level);

		for (var list : propLists.values()) {
			list.tick(updates);
		}

		updates.send(level);
	}

	@Override
	protected boolean isValid(PropSpawnType type) {
		return type != PropSpawnType.DUMMY && type != PropSpawnType.ASSETS;
	}
}
