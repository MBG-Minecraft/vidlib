package dev.latvian.mods.vidlib.feature.note;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public interface PublicNoteCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("public-note", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("text", StringArgumentType.greedyString())
			.executes(ctx -> note(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "text")))
		)
	);

	static int note(ServerPlayer player, String text) {
		player.server.s2c(new CreateNotePayload(new Note(player, text, NoteVisibility.PUBLIC)));
		return 1;
	}
}
