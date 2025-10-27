package dev.latvian.mods.vidlib.feature.skin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;

public interface SkinCommand {

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("skin", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("texture", ResourceLocationArgument.id())
					.executes(ctx -> setSkin(
						EntityArgument.getPlayers(ctx, "player"),
						ResourceLocationArgument.getId(ctx, "texture"),
						false,
						Optional.empty(),
						Optional.empty()
					))
					.then(Commands.argument("slim", BoolArgumentType.bool())
						.executes(ctx -> setSkin(
							EntityArgument.getPlayers(ctx, "player"),
							ResourceLocationArgument.getId(ctx, "texture"),
							BoolArgumentType.getBool(ctx, "slim"),
							Optional.empty(),
							Optional.empty()
						))
						.then(
							Commands.argument("cape", ResourceLocationArgument.id())
								.executes(ctx -> setSkin(
									EntityArgument.getPlayers(ctx, "player"),
									ResourceLocationArgument.getId(ctx, "texture"),
									BoolArgumentType.getBool(ctx, "slim"),
									Optional.of(ResourceLocationArgument.getId(ctx, "cape")),
									Optional.empty()
								))
								.then(
									Commands.argument("elytra", ResourceLocationArgument.id())
										.executes(ctx -> setSkin(
											EntityArgument.getPlayers(ctx, "player"),
											ResourceLocationArgument.getId(ctx, "texture"),
											BoolArgumentType.getBool(ctx, "slim"),
											Optional.of(ResourceLocationArgument.getId(ctx, "cape")),
											Optional.of(ResourceLocationArgument.getId(ctx, "elytra"))
										))
								)
						)
					)
				)
			)
		)
		.then(Commands.literal("remove")
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> removeSkin(EntityArgument.getPlayers(ctx, "player")))
			)
		)
	);

	private static int removeSkin(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.setClothing(Clothing.NONE);
		}

		return 1;
	}

	private static int setSkin(
		Collection<ServerPlayer> players,
		ResourceLocation texture,
		boolean slim,
		Optional<ResourceLocation> capeTexture,
		Optional<ResourceLocation> elytraTexture
	) {
		var skin = new VLSkin(texture, slim, capeTexture, elytraTexture);

		for (var player : players) {
			player.setSkin(skin);
		}

		return 1;
	}
}
