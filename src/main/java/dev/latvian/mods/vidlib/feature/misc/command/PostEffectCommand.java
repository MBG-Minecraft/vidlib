package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

public interface PostEffectCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("post-effect", (command, buildContext) -> command
		.requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.argument("id", IdentifierArgument.id())
			.executes(ctx -> setPostEffect(ctx.getSource(), IdentifierArgument.getId(ctx, "id")))
		)
	);

	private static int setPostEffect(CommandSourceStack source, Identifier id) throws CommandSyntaxException {
		source.getPlayerOrException().setPostEffect(id);
		return 1;
	}
}
