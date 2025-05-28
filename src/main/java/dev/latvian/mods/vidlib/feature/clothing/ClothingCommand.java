package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ClothingCommand {
	List<ResourceLocation> CLOTHING_IDS = new ArrayList<>();
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("clothing"), () -> CLOTHING_IDS);

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("clothing", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("clothing", ResourceLocationArgument.id())
					.suggests(SUGGESTION_PROVIDER)
					.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocationArgument.getId(ctx, "clothing")), ClothingParts.ALL)))
					.then(Commands.argument("parts", ClothingParts.REGISTERED_DATA_TYPE.argument(buildContext))
						.executes(ctx -> clothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocationArgument.getId(ctx, "clothing")), ClothingParts.REGISTERED_DATA_TYPE.get(ctx, "parts"))))
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
