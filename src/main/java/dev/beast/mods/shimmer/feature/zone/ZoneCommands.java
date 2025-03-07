package dev.beast.mods.shimmer.feature.zone;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.util.KnownCodec;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface ZoneCommands {
	static LiteralArgumentBuilder<CommandSourceStack> createCommand(CommandBuildContext buildContext) {
		return Commands.literal("zones")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.literal("show")
				.executes(ctx -> show(ctx.getSource().getPlayerOrException()))
			)
			.then(Commands.literal("render-type")
				.then(Commands.literal("normal")
					.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), 0, null))
				)
				.then(Commands.literal("collisions")
					.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), 1, null))
				)
				.then(Commands.literal("blocks")
					.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), 2, BlockFilter.NONE.instance()))
					.then(Commands.argument("filter", KnownCodec.BLOCK_FILTER.argument(buildContext))
						.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), 2, KnownCodec.BLOCK_FILTER.get(ctx, "filter")))

					)
				)
			);
	}

	private static int show(ServerPlayer player) {
		var data = player.get(InternalPlayerData.LOCAL);
		data.renderZones = !data.renderZones;
		data.setChanged();
		return 1;
	}

	private static int renderType(ServerPlayer player, int type, @Nullable BlockFilter filter) {
		var data = player.get(InternalPlayerData.LOCAL);
		data.zoneRenderType = type;

		if (filter != null) {
			data.zoneBlockFilter = filter;
		}

		data.setChanged();
		return 1;
	}
}
