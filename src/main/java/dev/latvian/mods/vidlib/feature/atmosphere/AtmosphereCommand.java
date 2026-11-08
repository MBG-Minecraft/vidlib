package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.Level;

public interface AtmosphereCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("atmosphere", (command, buildContext) -> command
		.requires(source -> Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("set")
			.then(Commands.argument("atmosphere", Atmosphere.DATA_TYPE.argument(buildContext))
				.executes(ctx -> atmosphere(ctx.getSource().getLevel(), Atmosphere.DATA_TYPE.get(ctx, "atmosphere")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> atmosphere(ctx.getSource().getLevel(), null))
		)
	);

	private static int atmosphere(Level level, Ref<Atmosphere> atmosphere) {
		LevelSessionData.of(level).setAtmosphere(atmosphere);
		return 1;
	}
}
