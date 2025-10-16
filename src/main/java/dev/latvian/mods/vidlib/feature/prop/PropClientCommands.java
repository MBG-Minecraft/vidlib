package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;

public interface PropClientCommands {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("client-prop", PropCommands.COMMAND);
}
