package dev.latvian.mods.vidlib.feature.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public interface NPCServerCommands {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (ctx, builder) -> {
		var input = builder.getRemaining().toLowerCase(Locale.ROOT);

		for (var id : NPCRecording.getReplay(ctx.getSource().registryAccess()).keySet()) {
			if (SharedSuggestionProvider.matchesSubStr(input, id)) {
				builder.suggest(id);
			}
		}

		return builder.buildFuture();
	};

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("summon-npc", (command, buildContext) -> {
		command.then(Commands.literal("as")
			.then(Commands.argument("as", GameProfileArgument.gameProfile())
				.then(Commands.argument("npc", StringArgumentType.string())
					.suggests(SUGGESTION_PROVIDER)
					.then(Commands.argument("at", Vec3Argument.vec3())
						.executes(ctx -> summonAs(ctx.getSource(), StringArgumentType.getString(ctx, "npc"), true, Vec3Argument.getVec3(ctx, "at"), GameProfileArgument.getGameProfiles(ctx, "as")))
					)
					.executes(ctx -> summonAs(ctx.getSource(), StringArgumentType.getString(ctx, "npc"), false, ctx.getSource().getPosition(), GameProfileArgument.getGameProfiles(ctx, "as")))
				)
			)
		);

		command.then(Commands.argument("npc", StringArgumentType.string())
			.suggests(SUGGESTION_PROVIDER)
			.then(Commands.argument("at", Vec3Argument.vec3())
				.executes(ctx -> summon(ctx.getSource(), StringArgumentType.getString(ctx, "npc"), true, Vec3Argument.getVec3(ctx, "at")))
			)
			.executes(ctx -> summon(ctx.getSource(), StringArgumentType.getString(ctx, "npc"), false, ctx.getSource().getPosition()))
		);
	});

	private static int summon(CommandSourceStack source, String npc, boolean relativePosition, Vec3 pos) {
		source.getLevel().s2c(new SummonNPCPayload(new NPCParticleOptions(npc, relativePosition, 0, Optional.empty()), pos));
		return 1;
	}

	private static int summonAs(CommandSourceStack source, String npc, boolean relativePosition, Vec3 pos, Collection<GameProfile> profiles) {
		var profile = profiles.stream().findFirst().orElse(null);

		if (profile == null) {
			return 0;
		}

		profile = source.getServer().getSessionService().fetchProfile(profile.getId(), true).profile();
		source.getLevel().s2c(new SummonNPCPayload(new NPCParticleOptions(npc, relativePosition, 0, Optional.of(profile)), pos));
		return 1;
	}
}
