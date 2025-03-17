package dev.beast.mods.shimmer.feature.clothing;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ClothingCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clothing", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("clothing", ShimmerResourceLocationArgument.id())
					.suggests(Clothing.SUGGESTION_PROVIDER)
					.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), Clothing.REGISTRY.unitValueMap().get(ShimmerResourceLocationArgument.getId(ctx, "clothing"))))
				)
			)
			.then(Commands.argument("clothing", ShimmerResourceLocationArgument.id())
				.suggests(Clothing.SUGGESTION_PROVIDER)
				.executes(ctx -> clothing(List.of(ctx.getSource().getPlayerOrException()), Clothing.REGISTRY.unitValueMap().get(ShimmerResourceLocationArgument.getId(ctx, "clothing"))))
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), null))
			)
			.executes(ctx -> clothing(List.of(ctx.getSource().getPlayerOrException()), null))
		)
	);

	private static int clothing(Collection<ServerPlayer> players, @Nullable Clothing clothing) {
		for (var player : players) {
			player.set(InternalPlayerData.CLOTHING, clothing == null ? Clothing.NONE : clothing);
		}

		return 1;
	}
}
