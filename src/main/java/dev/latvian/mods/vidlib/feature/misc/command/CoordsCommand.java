package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import net.minecraft.client.Minecraft;

public interface CoordsCommand {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("coords", (command, buildContext) -> command
		.executes(ctx -> coords())
	);

	private static int coords() {
		VidLibClientOptions.SHOW_COORDINATES.set(!VidLibClientOptions.getShowCoordinates());
		Minecraft.getInstance().options.save();
		return 1;
	}
}
