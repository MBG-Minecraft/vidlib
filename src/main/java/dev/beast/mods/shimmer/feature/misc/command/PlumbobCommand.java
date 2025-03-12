package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.icon.EmptyIcon;
import dev.beast.mods.shimmer.feature.icon.Icon;
import dev.beast.mods.shimmer.feature.icon.ItemIcon;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlumbobCommand {
	@AutoRegister
	ServerCommandHolder HOLDER = new ServerCommandHolder("plumbob", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("icon", Icon.KNOWN_CODEC.optionalArgument(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Icon.KNOWN_CODEC.getOptional(ctx, "icon")))
				)
			)
			.then(Commands.argument("icon", Icon.KNOWN_CODEC.optionalArgument(buildContext))
				.executes(ctx -> plumbob(List.of(ctx.getSource().getPlayerOrException()), Icon.KNOWN_CODEC.getOptional(ctx, "icon")))
			)
		)
		.then(Commands.literal("item")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("item", ItemArgument.item(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.of(new ItemIcon(ItemArgument.getItem(ctx, "item").createItemStack(1, true).copyWithCount(1)))))
				)
			)
			.then(Commands.argument("item", ItemArgument.item(buildContext))
				.executes(ctx -> plumbob(List.of(ctx.getSource().getPlayerOrException()), Optional.of(new ItemIcon(ItemArgument.getItem(ctx, "item").createItemStack(1, true).copyWithCount(1)))))
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> plumbob(EntityArgument.getPlayers(ctx, "player"), Optional.empty()))
			)
			.executes(ctx -> plumbob(List.of(ctx.getSource().getPlayerOrException()), Optional.empty()))
		)
	);

	private static int plumbob(Collection<ServerPlayer> players, Optional<Icon> icon) {
		for (var player : players) {
			player.set(InternalPlayerData.PLUMBOB, icon.orElse(EmptyIcon.INSTANCE).holder());
		}

		return 1;
	}
}
