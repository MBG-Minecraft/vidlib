package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.RegisteredDataType;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.Map;
import java.util.Objects;

public class CommandDataType<T> {
	private static final Map<DataType<?>, CommandDataType<?>> MAP = new Reference2ObjectOpenHashMap<>();

	public static final CommandDataType<Gradient> GRADIENT = CommandDataType.of(Gradient.DATA_TYPE);
	public static final CommandDataType<Shape> SHAPE = CommandDataType.of(Shape.DATA_TYPE);

	@SuppressWarnings("unchecked")
	public static <T> CommandDataType<T> of(DataType<T> dataType) {
		return (CommandDataType<T>) MAP.computeIfAbsent(dataType, CommandDataType::new);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ArgumentType enumArgument(Class<?> cl) {
		return EnumArgument.enumArgument((Class) cl);
	}

	public final DataType<T> dataType;
	public final Lazy<RegisteredDataType<T>> registeredDataType;

	private CommandDataType(DataType<T> dataType) {
		this.dataType = dataType;
		this.registeredDataType = Lazy.of(() -> {
			try {
				return Cast.to(Objects.requireNonNull(RegisteredDataType.BY_TYPE.get(dataType)));
			} catch (Exception ex) {
				throw new RuntimeException("DataType for '" + dataType.typeClass() + "' is not registered");
			}
		});
	}

	public ArgumentType<T> argument(CommandBuildContext ctx) {
		if (dataType.typeClass().isEnum()) {
			return enumArgument(dataType.typeClass());
		} else {
			var type = registeredDataType.get();

			if (type.argumentType() != null) {
				return type.argumentType().apply(type, ctx);
			}

			var ops = ctx.createSerializationContext(NbtOps.INSTANCE);
			return new DataArgumentType<>(ops, TagParser.create(ops), this);
		}
	}

	public T get(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, dataType.typeClass());
	}
}
