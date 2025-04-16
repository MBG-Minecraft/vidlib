package dev.latvian.mods.vidlib.util;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface MessageConsumer {
	static MessageConsumer ofPlayer(Player player) {
		return component -> player.displayClientMessage(component, false);
	}

	static MessageConsumer ofCommandSource(CommandSourceStack stack) {
		return new MessageConsumer() {
			@Override
			public void success(Component message) {
				stack.sendSuccess(() -> message, false);
			}

			@Override
			public void error(Component message) {
				stack.sendFailure(message);
			}
		};
	}

	void success(Component message);

	default void error(Component message) {
		success(Component.empty().withStyle(ChatFormatting.RED).append(message));
	}
}
