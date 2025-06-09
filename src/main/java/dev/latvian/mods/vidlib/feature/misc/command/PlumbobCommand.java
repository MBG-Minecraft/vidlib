package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.icon.EmptyIcon;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.ItemIcon;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;

public interface PlumbobCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("plumbob", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("icon", Icon.COMMAND.argument(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.of(Icon.COMMAND.get(ctx, "icon"))))
				)
			)
		)
		.then(Commands.literal("item")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("item", ItemArgument.item(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.of(new ItemIcon(ItemArgument.getItem(ctx, "item").createItemStack(1, true).copyWithCount(1)))))
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.empty()))
			)
		)
	);

	private static int plumbob(Collection<ServerPlayer> players, Optional<Icon> icon) {
		for (var player : players) {
			player.setPlumbob(icon.orElse(EmptyIcon.INSTANCE));
		}

		return 1;
	}
}
