package dev.latvian.mods.vidlib.feature.note;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public interface StaffNoteCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("staff-note", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("text", StringArgumentType.greedyString())
			.executes(ctx -> note(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "text")))
		)
	);

	static int note(ServerPlayer player, String text) {
		var payload = new CreateNotePayload(new Note(player, text, NoteVisibility.STAFF));

		for (var p : player.server.getPlayerList().getPlayers()) {
			if (p == player || p.isStaffOrTalent()) {
				p.s2c(payload);
			}
		}

		return 1;
	}
}
