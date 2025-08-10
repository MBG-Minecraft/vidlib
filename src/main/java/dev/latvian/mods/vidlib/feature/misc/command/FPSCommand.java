package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import net.minecraft.client.Minecraft;

public interface FPSCommand {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("fps", (command, buildContext) -> command
		.executes(ctx -> fps())
	);

	private static int fps() {
		VidLibClientOptions.SHOW_FPS.set(!VidLibClientOptions.getShowFPS());
		Minecraft.getInstance().options.save();
		return 1;
	}
}
