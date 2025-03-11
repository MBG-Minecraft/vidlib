package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public interface PlumbobCommand {
	@AutoRegister
	ServerCommandHolder HOLDER = new ServerCommandHolder("plumbob", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("item", ItemArgument.item(buildContext))
				.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), ItemArgument.getItem(ctx, "item").createItemStack(1, true)))
			)
		)
		.then(Commands.argument("item", ItemArgument.item(buildContext))
			.executes(ctx -> plumbob(List.of(ctx.getSource().getPlayerOrException()), ItemArgument.getItem(ctx, "item").createItemStack(1, true)))
		)
	);

	private static int plumbob(Collection<ServerPlayer> players, ItemStack item) {
		item = item.copyWithCount(1);

		for (var player : players) {
			player.set(InternalPlayerData.PLUMBOB, item);
		}

		return 1;
	}
}
