package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.session.ServerLevelSessionData;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class ServerProps extends Props<ServerLevel> {
	public ServerProps(ServerLevelSessionData levelData, ServerLevel level) {
		super(levelData, level);
	}

	@Override
	public void tick(boolean tick) {
		var updates = tick ? new S2CPacketBundleBuilder() : null;

		levelProps.tick(updates, tick);

		if (updates != null) {
			updates.send(PlayerTarget.of(level));
		}
	}

	@Override
	public void tickBukkit(@Nullable S2CPacketConsumer updates, boolean tick) {
		levelProps.tickBukkit(updates, tick);
	}
}
