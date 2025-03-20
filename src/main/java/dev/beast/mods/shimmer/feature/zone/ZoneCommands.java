package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface ZoneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("zones", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("show")
			.executes(ctx -> show(ctx.getSource().getPlayerOrException()))
		)
		.then(Commands.literal("render-type")
			.then(Commands.literal("normal")
				.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.NORMAL, null))
			)
			.then(Commands.literal("collisions")
				.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.COLLISIONS, null))
			)
			.then(Commands.literal("blocks")
				.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.BLOCKS, BlockFilter.NONE.instance()))
				.then(Commands.argument("filter", BlockFilter.KNOWN_CODEC.argument(buildContext))
					.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.BLOCKS, BlockFilter.KNOWN_CODEC.get(ctx, "filter")))

				)
			)
		)
	);

	private static int show(ServerPlayer player) {
		player.setShowZones(!player.getShowZones());
		return 1;
	}

	private static int renderType(ServerPlayer player, ZoneRenderType type, @Nullable BlockFilter filter) {
		player.setZoneRenderType(type);

		if (filter != null) {
			player.setZoneBlockFilter(filter);
		}

		return 1;
	}
}
