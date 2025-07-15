package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
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
		.then(Commands.literal("move")
			.then(Commands.argument("prop", IntegerArgumentType.integer(1))
				.then(Commands.argument("pos", Vec3Argument.vec3())
					.executes(ctx -> move(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "prop"), Vec3Argument.getCoordinates(ctx, "pos")))
				)
			)
		)
		.then(Commands.literal("rotate")
			.then(Commands.argument("prop", IntegerArgumentType.integer(1))
				.then(Commands.argument("rotation", RotationArgument.rotation())
					.executes(ctx -> rotate(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "prop"), RotationArgument.getRotation(ctx, "rotation")))
				)
			)
		)
	);

	static int spawn(CommandSourceStack source, ResourceLocation typeId, Vec3 pos, @Nullable CompoundTag initialData) {
		var type = PropType.ALL.get().get(typeId);

		if (type == null) {
			source.error("Prop type '" + typeId + "' not found!");
			return 0;
		}

		var props = source.getLevel().getProps();

		var propResult = props.create(props.context(type, PropSpawnType.USER, source.getLevel().getGameTime()), true, true, initialData == null ? null : source.getLevel().nbtOps(), initialData, prop -> {
			prop.setPos(pos.x, pos.y, pos.z);
			prop.onSpawned(source);
		});

		if (propResult.error().isPresent()) {
			source.error("Unable to create prop of type '" + typeId + "': " + propResult.error().get().message());
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
				prop.removeByCommand();
			}

			source.getLevel().s2c(new RemoveAllPropsPayload(list.type, PropRemoveType.COMMAND));
		} else {
			var type = PropType.ALL.get().get(typeId);

			if (type == null) {
				source.error("Prop type '" + typeId + "' not found!");
				return 0;
			}

			for (var prop : list) {
				if (prop.type == type) {
					prop.removeByCommand();
					killed++;
				}
			}
		}

		int k = killed;
		source.broadcast("Killed " + k + " props");
		return killed;
	}

	static int move(CommandSourceStack source, int propId, Coordinates coordinates) {
		var prop = source.getLevel().getProps().levelProps.get(propId);

		if (prop != null) {
			prop.setPos(coordinates.getPosition(prop.getCommandSourceAt(source)));
			prop.sync(Prop.POSITION);
			return 1;
		}

		return 0;
	}

	static int rotate(CommandSourceStack source, int propId, Coordinates coordinates) {
		var prop = source.getLevel().getProps().levelProps.get(propId);

		if (prop != null) {
			prop.setRot(Rotation.deg(coordinates.getRotation(prop.getCommandSourceAt(source))));
			prop.sync(Prop.YAW);
			prop.sync(Prop.PITCH);
			return 1;
		}

		return 0;
	}
}
