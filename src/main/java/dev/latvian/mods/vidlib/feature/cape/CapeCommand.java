package dev.latvian.mods.vidlib.feature.cape;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.skin.VLSkin;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;

public interface CapeCommand {

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("cape", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("texture", ResourceLocationArgument.id())
					.executes(ctx -> setSkin(
						EntityArgument.getPlayers(ctx, "player"),
						ResourceLocationArgument.getId(ctx, "texture")
					))
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> removeCape(EntityArgument.getPlayers(ctx, "player")))
			)
		)
	);

	private static int removeCape(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.setCape(null);
		}

		return 1;
	}

	private static int setSkin(
		Collection<ServerPlayer> players,
		ResourceLocation texture
	) {
		var cape = new VLCape(texture);

		for (var player : players) {
			player.setCape(cape);
		}

		return 1;
	}
}
