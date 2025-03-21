package dev.beast.mods.shimmer.feature.clothing;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Collection;

public interface ClothingCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clothing", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("clothing", ShimmerResourceLocationArgument.id())
					.suggests(Clothing.SUGGESTION_PROVIDER)
					.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceKey.create(EquipmentAssets.ROOT_ID, ShimmerResourceLocationArgument.getId(ctx, "clothing")), ClothingParts.ALL)))
					.then(Commands.argument("parts", ClothingParts.KNOWN_CODEC.argument(buildContext))
						.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceKey.create(EquipmentAssets.ROOT_ID, ShimmerResourceLocationArgument.getId(ctx, "clothing")), ClothingParts.KNOWN_CODEC.get(ctx, "parts"))))
					)
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), Clothing.NONE))
			)
		)
	);

	private static int clothing(Collection<ServerPlayer> players, Clothing clothing) {
		for (var player : players) {
			player.setClothing(clothing);
		}

		return 1;
	}
}
