package dev.latvian.mods.vidlib.feature.note;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;

public interface PrivateNoteCommand {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("private-note", (command, buildContext) -> command
		.then(Commands.argument("text", StringArgumentType.greedyString())
			.executes(ctx -> note(StringArgumentType.getString(ctx, "text")))
		)
	);

	static int note(String text) {
		var mc = Minecraft.getInstance();
		mc.player.vl$sessionData().createNote(new Note(mc.player, text, NoteVisibility.PRIVATE));
		return 1;
	}
}
