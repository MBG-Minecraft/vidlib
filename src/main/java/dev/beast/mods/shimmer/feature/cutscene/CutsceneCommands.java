package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.NbtOps;

public interface CutsceneCommands {
	static LiteralArgumentBuilder<CommandSourceStack> createCommand(CommandBuildContext buildContext) {
		return Commands.literal("cutscene")
			.then(Commands.literal("play")
				.then(Commands.argument("id", ResourceLocationArgument.id())
					.executes(ctx -> {
						ctx.getSource().getPlayerOrException().playCutscene(ResourceLocationArgument.getId(ctx, "id"));
						return 1;
					})
				)
			)
			.then(Commands.literal("create")
				.then(Commands.argument("data", CompoundTagArgument.compoundTag())
					.executes(ctx -> {
						var tag = CompoundTagArgument.getCompoundTag(ctx, "data");
						var ops = buildContext.createSerializationContext(NbtOps.INSTANCE);
						ctx.getSource().getPlayerOrException().playCutscene(Cutscene.CODEC.decode(ops, tag).getOrThrow().getFirst());
						return 1;
					})
				)
			)
			.then(Commands.literal("stop")
				.executes(ctx -> {
					ctx.getSource().getPlayerOrException().stopCutscene();
					return 1;
				})
			);
	}
}
