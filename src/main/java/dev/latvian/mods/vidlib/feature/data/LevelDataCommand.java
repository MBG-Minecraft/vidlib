package dev.latvian.mods.vidlib.feature.data;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.permissions.Permissions;

public interface LevelDataCommand {
	SuggestionProvider<CommandSourceStack> SESSION_NAMES = (ctx, builder) -> SharedSuggestionProvider.suggest(LevelSessionData.of(PlatformHelper.INSTANCE.getLevel(ctx.getSource())).globalData.levelMap.keySet().stream().map(ResourceKey::identifier).map(ID::idToString), builder);

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("level-data", (command, buildContext) -> {
		command.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));

		var levelCmd = Commands.argument("dimension", DimensionArgument.dimension());
		levelCmd.suggests(SESSION_NAMES);

		for (var key : DataKey.LEVEL.all.values()) {
			var cmd = Commands.literal(key.id);

			cmd.then(Commands.literal("get")
				.executes(ctx -> {
					var vl = LevelSessionData.of(DimensionArgument.getDimension(ctx, "dimension"));

					ctx.getSource().sendSuccess(() -> {
						var value = vl.dataMap.get(key);

						if (value == null) {
							return Component.literal(vl.dimension.identifier() + ": ").append(Component.literal("null").withColor(TextColor.RED));
						} else {
							var nbt = key.type.codec().encodeStart(vl.globalData.nbtOps, Cast.to(value)).getOrThrow();
							return Component.literal(vl.dimension.identifier() + ": ").append(NbtUtils.toPrettyComponent(nbt));
						}
					}, false);

					return 1;
				})
			);

			cmd.then(Commands.literal("set")
				.then(Commands.argument("value", key.type.argument(buildContext))
					.executes(ctx -> {
						var vl = LevelSessionData.of(DimensionArgument.getDimension(ctx, "dimension"));
						var value = key.type.get(ctx, "value");
						vl.dataMap.set(key, Cast.to(value));
						return 1;
					})
				)
			);

			cmd.then(Commands.literal("reset")
				.executes(ctx -> {
					var vl = LevelSessionData.of(DimensionArgument.getDimension(ctx, "dimension"));
					vl.dataMap.reset(key);
					return 1;
				})
			);

			levelCmd.then(cmd);
		}

		command.then(levelCmd);
	});
}
