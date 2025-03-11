package dev.beast.mods.shimmer.feature.entity;

public interface EntityOverrideCommands {
	/*
	@AutoRegister
	ServerCommandHolder HOLDER = new ServerCommandHolder("global-entity-override", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("map", )
				.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.NORMAL, null))
			)
			.then(Commands.literal("collisions")
				.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.COLLISIONS, null))
			)
			.then(Commands.literal("blocks")
				.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.BLOCKS, BlockFilter.NONE.instance()))
				.then(Commands.argument("filter", KnownCodec.BLOCK_FILTER.argument(buildContext))
					.executes(ctx -> renderType(ctx.getSource().getPlayerOrException(), ZoneRenderType.BLOCKS, KnownCodec.BLOCK_FILTER.get(ctx, "filter")))

				)
			)
		)
	);

	private static int show(ServerPlayer player) {
		player.set(InternalPlayerData.SHOW_ZONES, !player.get(InternalPlayerData.SHOW_ZONES));
		return 1;
	}

	private static int renderType(ServerPlayer player, ZoneRenderType type, @Nullable BlockFilter filter) {
		player.set(InternalPlayerData.ZONE_RENDER_TYPE, type);

		if (filter != null) {
			player.set(InternalPlayerData.ZONE_BLOCK_FILTER, filter);
		}

		return 1;
	}
	 */
}
