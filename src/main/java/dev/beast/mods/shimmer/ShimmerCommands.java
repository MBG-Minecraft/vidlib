package dev.beast.mods.shimmer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeCommands;
import dev.beast.mods.shimmer.feature.clock.ClockCommands;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneCommands;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.misc.RefreshNamePayload;
import dev.beast.mods.shimmer.feature.zone.ZoneCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ShimmerCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		dispatcher.register(ZoneCommands.createCommand());
		dispatcher.register(ClockCommands.createCommand());
		dispatcher.register(CutsceneCommands.createCommand(buildContext));
		dispatcher.register(CameraShakeCommands.createCommand(buildContext));

		dispatcher.register(Commands.literal("set-fake-block")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.then(Commands.argument("state", BlockStateArgument.block(buildContext))
					.executes(ctx -> setFakeBlock(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "pos"), BlockStateArgument.getBlock(ctx, "state").getState()))
				)
			)
		);

		dispatcher.register(Commands.literal("post-effect")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.argument("id", ResourceLocationArgument.id())
				.executes(ctx -> setPostEffect(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id")))
			)
		);

		dispatcher.register(Commands.literal("heal")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.argument("player", EntityArgument.players())
				.executes(ctx -> heal(EntityArgument.getOptionalPlayers(ctx, "player")))
			)
			.executes(ctx -> heal(List.of(ctx.getSource().getPlayerOrException())))
		);

		dispatcher.register(Commands.literal("plumbob")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("item", ItemArgument.item(buildContext))
					.executes(ctx -> plumbob(EntityArgument.getOptionalPlayers(ctx, "player"), ItemArgument.getItem(ctx, "item").createItemStack(1, true)))
				)
			)
			.then(Commands.argument("item", ItemArgument.item(buildContext))
				.executes(ctx -> plumbob(List.of(ctx.getSource().getPlayerOrException()), ItemArgument.getItem(ctx, "item").createItemStack(1, true)))
			)
		);

		dispatcher.register(Commands.literal("nickname")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("nickname", ComponentArgument.textComponent(buildContext))
					.executes(ctx -> nickname(EntityArgument.getOptionalPlayers(ctx, "player"), ComponentArgument.getComponent(ctx, "nickname")))
				)
			)
			.then(Commands.argument("nickname", ComponentArgument.textComponent(buildContext))
				.executes(ctx -> nickname(List.of(ctx.getSource().getPlayerOrException()), ComponentArgument.getComponent(ctx, "nickname")))
			)
		);
	}

	private static int setFakeBlock(CommandSourceStack source, BlockPos pos, BlockState state) {
		source.getLevel().setFakeBlock(pos, state);
		return 1;
	}

	private static int setPostEffect(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
		source.getPlayerOrException().setPostEffect(id);
		return 1;
	}

	private static int heal(Collection<ServerPlayer> players) {
		for (var player : players) {
			player.heal();
		}

		return 1;
	}

	private static int plumbob(Collection<ServerPlayer> players, ItemStack hat) {
		hat = hat.copyWithCount(1);

		for (var player : players) {
			var data = player.get(InternalPlayerData.GLOBAL);
			data.plumbob = hat;
			data.setChanged();
		}

		return 1;
	}

	private static int nickname(Collection<ServerPlayer> players, Component name) {
		for (var player : players) {
			var data = player.get(InternalPlayerData.GLOBAL);
			data.nickname = name.getString().isEmpty() ? Optional.empty() : Optional.of(name);
			data.setChanged();
			player.refreshDisplayName();
			player.level().s2c(new RefreshNamePayload(player.getUUID()));
		}

		return 1;
	}
}
