package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public interface InvisibleCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("invisible", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.player())
			.executes(ctx -> invisible(EntityArgument.getPlayer(ctx, "player")))
		)
		.executes(ctx -> invisible(ctx.getSource().getPlayerOrException()))
	);

	private static int invisible(ServerPlayer player) {
		if (player.hasEffect(MobEffects.INVISIBILITY)) {
			player.removeEffect(MobEffects.INVISIBILITY);
		} else {
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, false, false, false));
		}

		return 1;
	}
}
