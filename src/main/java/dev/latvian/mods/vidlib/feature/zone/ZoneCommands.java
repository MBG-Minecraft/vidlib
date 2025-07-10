package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ZoneCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("zones", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("count-blocks")
			.then(Commands.argument("id", ZoneContainer.COMMAND.argument(buildContext))
				.executes(ctx -> countBlocks(ctx.getSource(), ZoneContainer.COMMAND.get(ctx, "id"), BlockFilter.NONE.instance()))
				.then(Commands.argument("ignored-blocks", BlockFilter.COMMAND.argument(buildContext))
					.executes(ctx -> countBlocks(ctx.getSource(), ZoneContainer.COMMAND.get(ctx, "id"), BlockFilter.COMMAND.get(ctx, "ignored-blocks")))
				)
			)
		)
		.then(Commands.literal("save-structure")
			.then(Commands.argument("id", ZoneContainer.COMMAND.argument(buildContext))
				.executes(ctx -> saveStructure(ctx.getSource(), ZoneContainer.COMMAND.get(ctx, "id"), BlockFilter.NONE.instance()))
				.then(Commands.argument("ignored-blocks", BlockFilter.COMMAND.argument(buildContext))
					.executes(ctx -> saveStructure(ctx.getSource(), ZoneContainer.COMMAND.get(ctx, "id"), BlockFilter.COMMAND.get(ctx, "ignored-blocks")))
				)
			)
		)
	);

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
