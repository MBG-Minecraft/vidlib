package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.latvian.mods.klib.block.filter.BlockFilter;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.MessageConsumer;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.Locale;
import java.util.function.BiFunction;

public interface StructureCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("structure", (command, buildContext) -> command
		.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
		.then(Commands.literal("add-blocks")
			.then(Commands.argument("start", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.executes(ctx -> {
						var source = ctx.getSource();
						StructureCapture.CURRENT.getValue().addBlocks(source.getLevel(), MessageConsumer.ofCommandSource(source), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end"));
						return 1;
					})
				)
			)
		)
		.then(posCommand("pos1", 1))
		.then(posCommand("pos2", 2))
		.then(Commands.literal("reset")
			.executes(ctx -> {
				var source = ctx.getSource();
				source.sendSuccess(() -> Component.literal("Removed %,d blocks and cleared pos1/pos2".formatted(StructureCapture.CURRENT.getValue().blocks.size())), false);
				StructureCapture.CURRENT.setValue(new CurrentStructureCapture());
				StructureCapture.resetPos();
				return 1;
			})
		)
		.then(Commands.literal("set-ignore-filter")
			.then(Commands.argument("filter", BlockFilter.REGISTRY.createArgument(buildContext))
				.executes(ctx -> {
					@SuppressWarnings("unchecked")
					var filter = (Ref<BlockFilter>) ctx.getArgument("filter", Ref.class);
					StructureCapture.IGNORE_FILTER.setValue(filter);
					return 1;
				})
			)
		)
		.then(Commands.literal("fluids")
			.then(Commands.argument("fluids", BoolArgumentType.bool())
				.executes(ctx -> {
					StructureCapture.INCLUDE_FLUIDS.set(BoolArgumentType.getBool(ctx, "fluids"));
					return 1;
				})
			)
		)
		.then(Commands.literal("particles")
			.then(Commands.argument("particles", BoolArgumentType.bool())
				.executes(ctx -> {
					StructureCapture.PARTICLES.set(BoolArgumentType.getBool(ctx, "particles"));
					return 1;
				})
			)
		)
		.then(Commands.literal("save")
			.then(Commands.literal("ghost-chunks")
				.then(saveArgument((source, name) -> StructureCapture.capture(source.getLevel(), MessageConsumer.ofCommandSource(source), name)))
			)
			.then(Commands.literal("full")
				.then(saveArgument((source, name) -> StructureCapture.save(source.getLevel(), MessageConsumer.ofCommandSource(source), name, false)))
			)
			.then(Commands.literal("shell")
				.then(saveArgument((source, name) -> StructureCapture.save(source.getLevel(), MessageConsumer.ofCommandSource(source), name, true)))
			)
			.then(Commands.literal("schem")
				.then(saveArgument((source, name) -> StructureCapture.saveSchem(source.getLevel(), MessageConsumer.ofCommandSource(source), name)))
			)
		)
		.then(Commands.literal("move")
			.then(Commands.argument("to", BlockPosArgument.blockPos())
				.executes(ctx -> StructureCapture.move(BlockPosArgument.getBlockPos(ctx, "to")))
			)
		)
	);

	private static LiteralArgumentBuilder<CommandSourceStack> posCommand(String name, int index) {
		return Commands.literal(name)
			.executes(ctx -> setPos(ctx.getSource(), index, BlockPos.containing(ctx.getSource().getPosition())))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.executes(ctx -> setPos(ctx.getSource(), index, BlockPosArgument.getBlockPos(ctx, "pos")))
			);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> saveArgument(BiFunction<CommandSourceStack, String, Integer> action) {
		return Commands.argument("name", StringArgumentType.word())
			.executes(ctx -> action.apply(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT)));
	}

	static int setPos(CommandSourceStack source, int index, BlockPos pos) {
		StructureCapture.setPos(index, pos);
		source.sendSuccess(() -> Component.literal("Set pos%d to %d %d %d".formatted(index, pos.getX(), pos.getY(), pos.getZ())), false);
		return 1;
	}
}