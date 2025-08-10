package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface CanvasCommands {
	@ClientAutoRegister
	ClientCommandHolder COMMAND = new ClientCommandHolder("canvas", (command, buildContext) -> command
		.then(Commands.literal("preview")
			.then(Commands.literal("color")
				.then(Commands.argument("canvas", ResourceLocationArgument.id())
					.suggests((ctx, builder) -> {
						for (var holder : CanvasImpl.ENABLED) {
							builder.suggest(holder.idString);
						}

						return builder.buildFuture();
					})
					.executes(ctx -> preview(ctx.getSource(), ResourceLocationArgument.getId(ctx, "canvas"), false))
				)
			)
			.then(Commands.literal("depth")
				.then(Commands.argument("canvas", ResourceLocationArgument.id())
					.suggests((ctx, builder) -> {
						for (var holder : CanvasImpl.ENABLED) {
							builder.suggest(holder.idString);
						}

						return builder.buildFuture();
					})
					.executes(ctx -> preview(ctx.getSource(), ResourceLocationArgument.getId(ctx, "canvas"), true))
				)
			)
		)
	);

	static int preview(CommandSourceStack source, ResourceLocation id, boolean depth) {
		var c = CanvasImpl.get(id);

		if (c == null) {
			source.sendFailure(Component.literal("Canvas not found!"));
			return 0;
		}

		if (depth) {
			c.previewDepth = !c.previewDepth;
		} else {
			c.previewColor = !c.previewColor;
		}

		return 1;
	}
}
