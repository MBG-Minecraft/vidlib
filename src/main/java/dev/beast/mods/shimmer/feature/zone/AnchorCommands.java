package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.data.InternalServerData;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.math.AAIBB;
import dev.beast.mods.shimmer.math.Color;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;

public interface AnchorCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("anchor", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("show")
			.executes(ctx -> show(ctx.getSource()))
		)
		.then(Commands.literal("set")
			.then(Commands.argument("start", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.executes(ctx -> set(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end")))
				)
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> remove(ctx.getSource()))
		)
	);

	private static int show(CommandSourceStack source) {
		var areas = source.getServer().getServerData().get(InternalServerData.ANCHOR).shapes().get(source.getLevel().dimension());

		if (areas != null) {
			var positions = new ArrayList<BlockPos>();

			for (var area : areas) {
				area.forEveryEdgePosition(p -> positions.add(p.immutable()));
			}

			source.getLevel().cubeParticles(new CubeParticleOptions(Color.YELLOW, 200), positions);
		}

		return 1;
	}

	private static int set(CommandSourceStack source, BlockPos start, BlockPos end) {
		var list = new ArrayList<>(source.getServer().getServerData().get(InternalServerData.ANCHOR).areas());
		list.removeIf(area -> area.dimension() == source.getLevel().dimension());
		list.add(new Anchor.Area(source.getLevel().dimension(), new AAIBB(start, end)));
		source.getServer().getServerData().set(InternalServerData.ANCHOR, Anchor.create(list));
		source.getLevel().shimmer$updateLoadedChunks();
		return 1;
	}

	private static int remove(CommandSourceStack source) {
		var list = new ArrayList<>(source.getServer().getServerData().get(InternalServerData.ANCHOR).areas());
		list.removeIf(area -> area.dimension() == source.getLevel().dimension());
		source.getServer().getServerData().set(InternalServerData.ANCHOR, Anchor.create(list));
		source.getLevel().shimmer$updateLoadedChunks();
		return 1;
	}
}
