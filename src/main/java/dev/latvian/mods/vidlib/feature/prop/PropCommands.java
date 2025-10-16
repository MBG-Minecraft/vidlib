package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.brigadier.arguments.StringArgumentType;
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

import java.util.function.Predicate;

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
		.then(Commands.literal("remove")
			.then(Commands.literal("all")
				.executes(ctx -> remove(ctx.getSource(), prop -> true))
			)
			.then(Commands.literal("type")
				.then(Commands.argument("type", ResourceLocationArgument.id())
					.suggests(TYPE_SUGGESTION_PROVIDER)
					.executes(ctx -> remove(ctx.getSource(), PropType.ALL.get().get(ResourceLocationArgument.getId(ctx, "type"))))
				)
			)
			.then(Commands.literal("id")
				.then(Commands.argument("prop", StringArgumentType.word())
					.executes(ctx -> {
						int id = Integer.parseUnsignedInt(StringArgumentType.getString(ctx, "prop"), 16);
						return remove(ctx.getSource(), prop -> prop.id == id);
					})
				)
			)
		)
		.then(Commands.literal("move")
			.then(Commands.argument("prop", StringArgumentType.word())
				.then(Commands.argument("pos", Vec3Argument.vec3())
					.executes(ctx -> {
						int id = Integer.parseUnsignedInt(StringArgumentType.getString(ctx, "prop"), 16);
						return move(ctx.getSource(), id, Vec3Argument.getCoordinates(ctx, "pos"));
					})
				)
			)
		)
		.then(Commands.literal("rotate")
			.then(Commands.argument("prop", StringArgumentType.word())
				.then(Commands.argument("rotation", RotationArgument.rotation())
					.executes(ctx -> {
						int id = Integer.parseUnsignedInt(StringArgumentType.getString(ctx, "prop"), 16);
						return rotate(ctx.getSource(), id, RotationArgument.getRotation(ctx, "rotation"));
					})
				)
			)
		)
		.then(Commands.literal("clone")
			.then(Commands.argument("prop", StringArgumentType.word())
				.executes(ctx -> {
					int id = Integer.parseUnsignedInt(StringArgumentType.getString(ctx, "prop"), 16);
					return clone(ctx.getSource(), id);
				})
			)
		)
		.then(Commands.literal("pause")
			.then(Commands.argument("prop", StringArgumentType.word())
				.executes(ctx -> {
					int id = Integer.parseUnsignedInt(StringArgumentType.getString(ctx, "prop"), 16);
					return pause(ctx.getSource(), id, true);
				})
			)
		)
		.then(Commands.literal("unpause")
			.then(Commands.argument("prop", StringArgumentType.word())
				.executes(ctx -> {
					int id = Integer.parseUnsignedInt(StringArgumentType.getString(ctx, "prop"), 16);
					return pause(ctx.getSource(), id, false);
				})
			)
		)
	);

	static int spawn(CommandSourceStack source, ResourceLocation typeId, Vec3 pos, @Nullable CompoundTag initialData) {
		var type = PropType.ALL.get().get(typeId);

		if (type == null) {
			source.error("Prop type '" + typeId + "' not found!");
			return 0;
		}

		var level = source.getSidedLevel();
		var props = level.getProps();

		var propResult = props.create(props.context(type, PropSpawnType.USER, level.getGameTime()), true, true, initialData == null ? null : level.nbtOps(), initialData, prop -> {
			if (level.isClientSide()) {
				prop.clientSideOnly = true;
			}

			prop.setPos(pos.x, pos.y, pos.z);
			prop.onSpawned(source);
		});

		if (propResult.error().isPresent()) {
			source.error("Unable to create prop of type '" + typeId + "': " + propResult.error().get().message());
			return 0;
		}

		return 1;
	}

	static int remove(CommandSourceStack source, Predicate<Prop> predicate) {
		var level = source.getSidedLevel();
		var props = level.getProps();
		var list = props.propLists.get(PropListType.LEVEL);
		int killed = list.removeAll(PropRemoveType.COMMAND, predicate);
		source.broadcast("Removed " + killed + " props");
		return killed;
	}

	static int move(CommandSourceStack source, int propId, Coordinates coordinates) {
		var level = source.getSidedLevel();
		var props = level.getProps();
		var prop = props.levelProps.get(propId);

		if (prop != null) {
			prop.setPos(coordinates.getPosition(prop.getCommandSourceAt(source)));
			prop.sync(Prop.POSITION);
			return 1;
		}

		return 0;
	}

	static int rotate(CommandSourceStack source, int propId, Coordinates coordinates) {
		var level = source.getSidedLevel();
		var props = level.getProps();
		var prop = props.levelProps.get(propId);

		if (prop != null) {
			prop.setRot(Rotation.deg(coordinates.getRotation(prop.getCommandSourceAt(source))));
			prop.sync(Prop.YAW);
			prop.sync(Prop.PITCH);
			return 1;
		}

		return 0;
	}

	static int clone(CommandSourceStack source, int propId) {
		var level = source.getSidedLevel();
		var props = level.getProps();
		var prop = props.levelProps.get(propId);

		if (prop != null) {
			return prop.copy().ifSuccess(props::add).isSuccess() ? 1 : 0;
		}

		return 0;
	}

	static int pause(CommandSourceStack source, int propId, boolean paused) {
		var level = source.getSidedLevel();
		var props = level.getProps();
		var prop = props.levelProps.get(propId);

		if (prop != null) {
			prop.setPausedAndSync(paused);
			return 1;
		}

		return 0;
	}
}
