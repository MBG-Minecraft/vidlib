package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.util.MessageConsumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public interface GhostChunkCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("ghost-chunks", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("add-blocks")
			.then(Commands.argument("start", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.executes(ctx -> {
						var start = BlockPosArgument.getBlockPos(ctx, "start");
						var end = BlockPosArgument.getBlockPos(ctx, "end");

						var minX = Math.min(start.getX(), end.getX());
						var minY = Math.min(start.getY(), end.getY());
						var minZ = Math.min(start.getZ(), end.getZ());
						var maxX = Math.max(start.getX(), end.getX());
						var maxY = Math.max(start.getY(), end.getY());
						var maxZ = Math.max(start.getZ(), end.getZ());
						var volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
						ctx.getSource().sendSuccess(() -> Component.literal("Scanning %,d block area...".formatted(volume)), false);
						var capture = StructureHolder.capture(ctx.getSource().getLevel(), start, end, GhostStructureCapture.FILTER.getValue(), true).withoutInvisibleBlocks();
						GhostStructureCapture.CURRENT.getValue().blocks.putAll(capture.offset(start).blocks());
						ctx.getSource().sendSuccess(() -> Component.literal("Added %,d blocks".formatted(capture.blocks().size())), false);
						return 1;
					})
				)
			)
		)
		.then(Commands.literal("reset")
			.executes(ctx -> {
				ctx.getSource().sendSuccess(() -> Component.literal("Removed %,d blocks".formatted(GhostStructureCapture.CURRENT.getValue().blocks.size())), false);
				GhostStructureCapture.CURRENT.setValue(new CurrentGhostStructureCapture());
				return 1;
			})
		)
		.then(Commands.literal("set-filter")
			.then(Commands.argument("filter", BlockFilter.REGISTERED_DATA_TYPE.argument(buildContext))
				.executes(ctx -> {
					GhostStructureCapture.FILTER.setValue(BlockFilter.REGISTERED_DATA_TYPE.get(ctx, "filter"));
					return 1;
				})
			)
		)
		.then(Commands.literal("capture")
			.then(Commands.argument("name", StringArgumentType.word())
				.executes(ctx -> capture(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT)))
			)
		)
		.then(Commands.literal("save")
			.then(Commands.argument("name", StringArgumentType.word())
				.executes(ctx -> save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), false))
				.then(Commands.argument("shell", BoolArgumentType.bool())
					.executes(ctx -> save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BoolArgumentType.getBool(ctx, "shell")))
				)
			)
		)
		.then(Commands.literal("move")
			.then(Commands.argument("to", BlockPosArgument.blockPos())
				.executes(ctx -> GhostStructureCapture.move(BlockPosArgument.getBlockPos(ctx, "to")))
			)
		)
	);

	static int capture(CommandSourceStack source, String name) {
		return GhostStructureCapture.capture(MessageConsumer.ofCommandSource(source), name);
	}

	static int save(CommandSourceStack source, String name, boolean createShell) {
		return GhostStructureCapture.save(MessageConsumer.ofCommandSource(source), name, createShell);
	}
}
