package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.prop.builtin.npc.NPCProp;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public interface CreateNPCCloneCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("create-npc-clone", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("target", EntityArgument.player())
			.executes(ctx -> createNPCClone(EntityArgument.getPlayer(ctx, "target")))
		)
	);

	private static int createNPCClone(ServerPlayer player) {
		NPCProp.createCloneFrom(player);
		return 1;
	}
}
