package dev.beast.mods.shimmer.feature.npc;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ClientCommandHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Locale;

public interface NPCCommands {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (ctx, builder) -> {
		var input = builder.getRemaining().toLowerCase(Locale.ROOT);

		for (var id : NPCRecording.getReplay(Minecraft.getInstance().level.registryAccess()).keySet()) {
			if (SharedSuggestionProvider.matchesSubStr(input, id)) {
				builder.suggest(id);
			}
		}

		return builder.buildFuture();
	};

	@AutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("npc", (command, buildContext) -> {
		command.then(Commands.literal("record")
			.then(Commands.argument("username", StringArgumentType.word())
				.executes(ctx -> {
					var mc = Minecraft.getInstance();
					var profile = SkullBlockEntity.fetchGameProfile(StringArgumentType.getString(ctx, "username")).join().get();

					mc.status("3...");
					mc.schedule(20, () -> mc.status("2..."));
					mc.schedule(40, () -> mc.status("1..."));
					mc.schedule(60, () -> {
						mc.status("Go!");
						mc.player.shimmer$sessionData().startNPCRecording(mc, profile);
					});

					return 1;
				})
			)
		);

		command.then(Commands.literal("stop-recording")
			.executes(ctx -> {
				var mc = Minecraft.getInstance();
				mc.player.shimmer$sessionData().stopNPCRecording(mc);
				return 1;
			})
		);

		command.then(Commands.literal("replay")
			.executes(ctx -> {
				var mc = Minecraft.getInstance();
				mc.player.shimmer$sessionData().replayNPCRecording(mc);
				return 1;
			})
		);
	});
}
