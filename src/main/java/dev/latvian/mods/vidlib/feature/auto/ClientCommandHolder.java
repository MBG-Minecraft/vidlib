package dev.latvian.mods.vidlib.feature.auto;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Predicate;

public record ClientCommandHolder(String name, Callback callback) {
	public interface Callback {
		void register(LiteralArgumentBuilder<CommandSourceStack> command, CommandBuildContext buildContext);
	}

	public ClientCommandHolder(String name, ServerCommandHolder holder) {
		this(name, (command, context) -> {
			holder.callback().register(command, context);
			command.requires(isClientSingleplayer());
		});
	}

	public static Predicate<CommandSourceStack> isClientSingleplayer() {
		return source -> {
			var level = PlatformHelper.CURRENT.getLevel(source);
			return level.isClientSide() && PlatformHelper.CURRENT.isLocalServer(level);
		};
	}
}
