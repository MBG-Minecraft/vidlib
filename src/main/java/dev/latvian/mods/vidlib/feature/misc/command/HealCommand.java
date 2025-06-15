package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;
import java.util.List;

public interface HealCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("heal", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("target", EntityArgument.entities())
			.executes(ctx -> heal(EntityArgument.getEntities(ctx, "target")))
		)
		.executes(ctx -> heal(List.of(ctx.getSource().getPlayerOrException())))
	);

	private static int heal(Collection<? extends Entity> targets) {
		for (var entity : targets) {
			if (entity instanceof LivingEntity living) {
				living.heal();
			}
		}

		return 1;
	}
}
