package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.RegisteredDataType;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class CommandDataType<T> {
	private static final Map<DataType<?>, CommandDataType<?>> MAP = new Reference2ObjectOpenHashMap<>();

	public static final CommandDataType<Gradient> GRADIENT = CommandDataType.of(Gradient.DATA_TYPE);
	public static final CommandDataType<Shape> SHAPE = CommandDataType.of(Shape.DATA_TYPE);

	@SuppressWarnings("unchecked")
	public static <T> CommandDataType<T> of(DataType<T> dataType) {
		return (CommandDataType<T>) MAP.computeIfAbsent(dataType, CommandDataType::new);
	}

	public final DataType<T> dataType;
	private final Lazy<RegisteredDataType<T>> registeredDataType;
	public SuggestionProvider suggestionProvider;

	private CommandDataType(DataType<T> dataType) {
		this.dataType = dataType;
		this.registeredDataType = Lazy.of(() -> {
			try {
				return Cast.to(Objects.requireNonNull(RegisteredDataType.BY_TYPE.get(dataType)));
			} catch (Exception ex) {
				VidLib.LOGGER.warn("DataType for '" + dataType.typeClass() + "' is not registered");
				return null;
			}
		});
	}

	@Nullable
	public RegisteredDataType<T> getRegisteredDataType() {
		return registeredDataType.get();
	}

	public VLRegistry<T> getRegistryArgumentType() {
		return (VLRegistry) getRegisteredDataType().argumentType();
	}

	public CommandDataType<T> suggests(SuggestionProvider provider) {
		this.suggestionProvider = provider;
		return this;
	}

	public CommandDataType<T> suggestsIDs(Supplier<Iterable<ResourceLocation>> allIds) {
		return suggests((ctx, builder) -> ID.suggest(builder, allIds));
	}

	public ArgumentType<?> argument(CommandBuildContext buildContext) {
		var type = getRegisteredDataType();

		if (type != null && type.argumentType() != null) {
			return type.argumentType().create(type, buildContext);
		}

		var ops = buildContext.createSerializationContext(NbtOps.INSTANCE);
		return new DataArgumentType<>(ops, TagParser.create(ops), this);
	}

	public T get(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		var type = getRegisteredDataType();

		if (type != null && type.argumentGetter() != null) {
			return type.argumentGetter().get(ctx, name);
		}

		return ctx.getArgument(name, dataType.typeClass());
	}
}
