package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

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
					.executes(ctx -> setClothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceLocationArgument.getId(ctx, "clothing"), ClothingParts.ALL)))
					.then(Commands.argument("parts", ClothingParts.COMMAND.argument(buildContext))
						.executes(ctx -> setClothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceLocationArgument.getId(ctx, "clothing"), ClothingParts.COMMAND.get(ctx, "parts"))))
					)
				)
			)
		)
		.then(Commands.literal("add")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("clothing", ResourceLocationArgument.id())
					.suggests(SUGGESTION_PROVIDER)
					.executes(ctx -> addClothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceLocationArgument.getId(ctx, "clothing"), ClothingParts.ALL)))
					.then(Commands.argument("parts", ClothingParts.COMMAND.argument(buildContext))
						.executes(ctx -> addClothing(EntityArgument.getPlayers(ctx, "player"), new Clothing(ResourceLocationArgument.getId(ctx, "clothing"), ClothingParts.COMMAND.get(ctx, "parts"))))
					)
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> setClothing(EntityArgument.getPlayers(ctx, "player"), Clothing.NONE))
			)
		)
	);

	private static int setClothing(Collection<ServerPlayer> players, Clothing clothing) {
		for (var player : players) {
			player.setClothing(clothing == Clothing.NONE ? List.of() : List.of(clothing));
		}

		return 1;
	}

	private static int addClothing(Collection<ServerPlayer> players, Clothing clothing) {
		if (clothing == Clothing.NONE) {
			return 0;
		}

		for (var player : players) {
			var list = new ArrayList<>(player.get(InternalPlayerData.CLOTHING));
			list.add(clothing);
			player.setClothing(list);
		}

		return 1;
	}
}
