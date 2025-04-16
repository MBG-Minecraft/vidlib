package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public interface SkyboxCommand {
	List<ResourceLocation> SKYBOX_IDS = new ArrayList<>();
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("skybox"), () -> SKYBOX_IDS);

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("skybox", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("set")
			.then(Commands.argument("skybox", ResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> skybox(ctx.getSource().getServer(), ResourceLocationArgument.getId(ctx, "skybox")))
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> skybox(ctx.getSource().getServer(), Skyboxes.DAY_WITH_CELESTIALS))
		)
	);

	private static int skybox(MinecraftServer server, ResourceLocation skybox) {
		server.setSkybox(skybox);
		return 1;
	}
}
