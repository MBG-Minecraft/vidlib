package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public interface DiscardCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("discard", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("entity", EntityArgument.entities())
			.executes(ctx -> discard(ctx.getSource().getLevel(), EntityArgument.getEntities(ctx, "entity")))
		)
	);

	private static int discard(ServerLevel level, Collection<? extends Entity> entities) {
		for (var entity : entities) {
			if (entity instanceof Player) {
				entity.kill(level);
			} else {
				entity.discard();
			}
		}

		return 1;
	}
}
