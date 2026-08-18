package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.util.MessageConsumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface VLCommandSourceStack extends MessageConsumer {
	@Override
	default void tell(Component message) {
		((CommandSourceStack) this).sendSuccess(() -> message, false);
	}

	default void broadcast(Component message) {
		((CommandSourceStack) this).sendSuccess(() -> message, true);
	}

	default void broadcast(String message) {
		broadcast(Component.literal(message));
	}

	@Override
	default void error(Component message) {
		((CommandSourceStack) this).sendFailure(message);
	}

	@Override
	default void status(Component message) {
		var entity = ((CommandSourceStack) this).getEntity();

		if (entity instanceof Player player) {
			player.status(message);
		} else {
			tell(message);
		}
	}
}
