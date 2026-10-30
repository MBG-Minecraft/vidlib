package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.icon.ItemIcon;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Collection;
import java.util.Optional;

public interface PlumbobCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("plumbob", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("icon", Icon.DATA_TYPE.argument(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.of(Icon.DATA_TYPE.get(ctx, "icon"))))
				)
			)
		)
		.then(Commands.literal("item")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("item", ItemArgument.item(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.of(new ItemIcon(ItemArgument.getItem(ctx, "item").createItemStack(1)).ref())))
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.empty()))
			)
		)
	);

	private static int plumbob(Collection<ServerPlayer> players, Optional<Ref<Icon>> icon) {
		for (var player : players) {
			player.setPlumbob(icon.orElse(Icon.EMPTY));
		}

		return 1;
	}
}
