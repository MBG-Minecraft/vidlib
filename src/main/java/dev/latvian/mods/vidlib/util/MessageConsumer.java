package dev.latvian.mods.vidlib.util;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface MessageConsumer {
	MessageConsumer IGNORE = new MessageConsumer() {
		@Override
		public void tell(Component message) {
		}

		@Override
		public void status(Component message) {
		}

		@Override
		public void error(Component message) {
		}
	};

	static MessageConsumer ofCommandSource(CommandSourceStack stack) {
		return new MessageConsumer() {
			@Override
			public void tell(Component message) {
				stack.sendSuccess(() -> message, false);
			}

			@Override
			public void error(Component message) {
				stack.sendFailure(message);
			}
		};
	}

	static MessageConsumer ofBroadcastCommandSource(CommandSourceStack stack) {
		return new MessageConsumer() {
			@Override
			public void tell(Component message) {
				stack.sendSuccess(() -> message, true);
			}

			@Override
			public void error(Component message) {
				stack.sendFailure(message);
			}
		};
	}

	void tell(Component message);

	default void tell(String message) {
		tell(Component.literal(message));
	}

	default void status(Component message) {
		tell(message);
	}

	default void status(String message) {
		status(Component.literal(message));
	}

	default void error(Component message) {
		tell(Component.empty().withStyle(ChatFormatting.RED).append(message));
	}

	default void error(String message) {
		error(Component.literal(message));
	}
}
