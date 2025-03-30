package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ZoneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("zones", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
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
		.then(Commands.literal("count-blocks")
			.then(Commands.argument("id", ZoneContainer.KNOWN_CODEC.argument(buildContext))
				.suggests(ZoneContainer.REGISTRY.suggestionProvider)
				.executes(ctx -> countBlocks(ctx.getSource(), ZoneContainer.KNOWN_CODEC.get(ctx, "id"), BlockFilter.NONE.instance()))
				.then(Commands.argument("ignored-blocks", BlockFilter.KNOWN_CODEC.argument(buildContext))
					.executes(ctx -> countBlocks(ctx.getSource(), ZoneContainer.KNOWN_CODEC.get(ctx, "id"), BlockFilter.KNOWN_CODEC.get(ctx, "ignored-blocks")))
				)
			)
		)
		.then(Commands.literal("save-structure")
			.then(Commands.argument("id", ZoneContainer.KNOWN_CODEC.argument(buildContext))
				.suggests(ZoneContainer.REGISTRY.suggestionProvider)
				.executes(ctx -> saveStructure(ctx.getSource(), ZoneContainer.KNOWN_CODEC.get(ctx, "id"), BlockFilter.NONE.instance()))
				.then(Commands.argument("ignored-blocks", BlockFilter.KNOWN_CODEC.argument(buildContext))
					.executes(ctx -> saveStructure(ctx.getSource(), ZoneContainer.KNOWN_CODEC.get(ctx, "id"), BlockFilter.KNOWN_CODEC.get(ctx, "ignored-blocks")))
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

	class BlockProcessor {
		public final Level level;
		public final BlockFilter filter;
		public final ZoneContainer zoneContainer;
		public long count = 0L;
		public int minX, minY, minZ, maxX, maxY, maxZ;

		public BlockProcessor(Level level, BlockFilter filter, ZoneContainer zoneContainer) {
			this.level = level;
			this.filter = filter;
			this.zoneContainer = zoneContainer;
		}

		public void process(BlockPos pos) {
			var state = level.getBlockState(pos);

			if (state.isAir() || filter.test(level, pos, state)) {
				return;
			}

			if (count == 0L) {
				minX = maxX = pos.getX();
				minY = maxY = pos.getY();
				minZ = maxZ = pos.getZ();
			} else {
				minX = Math.min(minX, pos.getX());
				minY = Math.min(minY, pos.getY());
				minZ = Math.min(minZ, pos.getZ());
				maxX = Math.max(maxX, pos.getX());
				maxY = Math.max(maxY, pos.getY());
				maxZ = Math.max(maxZ, pos.getZ());
			}

			count++;
		}

		public void processBlock(BlockPos pos, BlockState state) {
		}

		public void complete(CommandSourceStack source) {
			source.sendSuccess(() -> Component.literal("%,d blocks".formatted(count)), false);
		}
	}

	class SaveBlockProcessor extends BlockProcessor {
		public SaveBlockProcessor(Level level, BlockFilter filter, ZoneContainer zoneContainer) {
			super(level, filter, zoneContainer);
		}

		@Override
		public void processBlock(BlockPos pos, BlockState state) {
			super.processBlock(pos, state);
		}

		@Override
		public void complete(CommandSourceStack source) {
			source.sendSuccess(() -> Component.literal("%,d blocks".formatted(count)), false);
		}
	}

	private static int countBlocks(CommandSourceStack source, ZoneContainer container, BlockFilter filter) {
		var processor = new BlockProcessor(source.getServer().getLevel(container.dimension), filter, container);

		for (var zone : container.zones) {
			zone.zone.shape().getBlocks().forEach(processor::process);
		}

		processor.complete(source);
		return 1;
	}

	private static int saveStructure(CommandSourceStack source, ZoneContainer container, BlockFilter filter) {
		var processor = new SaveBlockProcessor(source.getServer().getLevel(container.dimension), filter, container);

		for (var zone : container.zones) {
			zone.zone.shape().getBlocks().forEach(processor::process);
		}

		processor.complete(source);
		return 1;
	}
}
