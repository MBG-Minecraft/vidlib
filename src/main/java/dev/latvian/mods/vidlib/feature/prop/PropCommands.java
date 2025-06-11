package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface PropCommands {
	SuggestionProvider<CommandSourceStack> TYPE_SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("prop_type"), () -> PropType.ALL.get().keySet());

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("prop", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("spawn")
			.then(Commands.argument("prop", ResourceLocationArgument.id())
				.suggests(TYPE_SUGGESTION_PROVIDER)
				.executes(ctx -> spawn(ctx.getSource(), ResourceLocationArgument.getId(ctx, "prop"), ctx.getSource().getPosition(), null))
				.then(Commands.argument("pos", Vec3Argument.vec3())
					.executes(ctx -> spawn(ctx.getSource(), ResourceLocationArgument.getId(ctx, "prop"), Vec3Argument.getVec3(ctx, "pos"), null))
					.then(Commands.argument("data", CompoundTagArgument.compoundTag())
						.executes(ctx -> spawn(ctx.getSource(), ResourceLocationArgument.getId(ctx, "prop"), Vec3Argument.getVec3(ctx, "pos"), CompoundTagArgument.getCompoundTag(ctx, "data")))
					)
				)
			)
		)
		.then(Commands.literal("kill")
			.then(Commands.literal("all")
				.executes(ctx -> kill(ctx.getSource(), null))
			)
			.then(Commands.argument("prop", ResourceLocationArgument.id())
				.suggests(TYPE_SUGGESTION_PROVIDER)
				.executes(ctx -> kill(ctx.getSource(), ResourceLocationArgument.getId(ctx, "prop")))
			)
		)
	);

	static int spawn(CommandSourceStack source, ResourceLocation typeId, Vec3 pos, @Nullable CompoundTag initialData) {
		var type = PropType.ALL.get().get(typeId);

		if (type == null) {
			source.sendFailure(Component.literal("Prop type '" + typeId + "' not found!"));
			return 0;
		}

		var props = source.getLevel().getProps();

		var propResult = props.create(props.context(type, PropSpawnType.USER, source.getLevel().getGameTime()), true, true, initialData == null ? null : source.getLevel().nbtOps(), initialData, prop -> {
			prop.setPos(pos.x, pos.y, pos.z);
			prop.onSpawned(source);
		});

		if (propResult.error().isPresent()) {
			source.sendFailure(Component.literal("Unable to create prop of type '" + typeId + "': " + propResult.error().get().message()));
			return 0;
		}

		return 1;
	}

	static int kill(CommandSourceStack source, @Nullable ResourceLocation typeId) {
		int killed = 0;
		var list = source.getLevel().getProps().propLists.get(PropListType.LEVEL);

		if (typeId == null) {
			killed = list.size();

			for (var prop : list) {
				prop.removed = PropRemoveType.COMMAND;
			}

			source.getLevel().s2c(new RemoveAllPropsPayload(list.type, PropRemoveType.COMMAND));
		} else {
			var type = PropType.ALL.get().get(typeId);

			if (type == null) {
				source.sendFailure(Component.literal("Prop type '" + typeId + "' not found!"));
				return 0;
			}

			for (var prop : list) {
				if (prop.type == type) {
					prop.removed = PropRemoveType.COMMAND;
					killed++;
				}
			}
		}

		int k = killed;
		source.sendSuccess(() -> Component.literal("Killed " + k + " props"), true);
		return killed;
	}
}
