package dev.latvian.mods.vidlib.feature.auto;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public record ClientCommandHolder(String name, Callback callback) {
	public interface Callback {
		void register(LiteralArgumentBuilder<CommandSourceStack> command, CommandBuildContext buildContext);
	}

	public ClientCommandHolder(String name, ServerCommandHolder holder) {
		this(name, (command, context) -> holder.callback().register(command, context));
	}
}
