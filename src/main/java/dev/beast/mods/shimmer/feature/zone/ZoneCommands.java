package dev.beast.mods.shimmer.feature.zone;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public interface ZoneCommands {
	static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
		return Commands.literal("zones")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(Commands.literal("show")
				.executes(ctx -> show(ctx.getSource().getPlayerOrException()))
			);
	}

	private static int show(ServerPlayer player) {
		var data = player.get(InternalPlayerData.LOCAL);
		data.renderZones = !data.renderZones;
		data.setChanged();
		return 1;
	}
}
