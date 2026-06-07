package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ClothingCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clothing", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.literal("custom")
					.then(Commands.argument("clothing", ClothingSet.COMMAND.argument(buildContext))
						.executes(ctx -> setClothing(EntityArgument.getPlayers(ctx, "player"), PlayerClothing.custom(ClothingSet.COMMAND.get(ctx, "clothing"))))
					)
				)
				.then(Commands.argument("clothing", ResourceLocationArgument.id())
					.suggests(ClothingPresets.SUGGESTION_PROVIDER)
					.executes(ctx -> setClothing(EntityArgument.getPlayers(ctx, "player"), PlayerClothing.preset(ClothingPresets.createId(ResourceLocationArgument.getId(ctx, "clothing")))))
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> removeClothing(EntityArgument.getPlayers(ctx, "player")))
			)
		)
	);

	private static int setClothing(Collection<ServerPlayer> players, @Nullable PlayerClothing clothing) {
		for (var player : players) {
			player.setClothing(clothing == null ? PlayerClothing.NONE : clothing);
		}

		return 1;
	}

	private static int removeClothing(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.setClothing(PlayerClothing.NONE);
		}

		return 1;
	}
}
