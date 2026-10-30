package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;

public interface PostEffectCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("post-effect", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.argument("id", IdentifierArgument.id())
			.executes(ctx -> setPostEffect(ctx.getSource(), IdentifierArgument.getId(ctx, "id")))
		)
	);

	private static int setPostEffect(CommandSourceStack source, Identifier id) throws CommandSyntaxException {
		source.getPlayerOrException().setPostEffect(id);
		return 1;
	}
}
