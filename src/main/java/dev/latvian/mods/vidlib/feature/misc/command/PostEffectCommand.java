package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

public interface PostEffectCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("post-effect", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("id", ResourceLocationArgument.id())
			.executes(ctx -> setPostEffect(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id")))
		)
	);

	private static int setPostEffect(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
		source.getPlayerOrException().setPostEffect(id);
		return 1;
	}
}
