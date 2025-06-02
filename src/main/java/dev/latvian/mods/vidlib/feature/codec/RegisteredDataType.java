package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.latvian.mods.kmath.MovementType;
import dev.latvian.mods.kmath.Range;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.shape.Shape;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.Cast;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public record RegisteredDataType<T>(
	ResourceLocation id,
	DataType<T> type,
	BiFunction<RegisteredDataType<T>, CommandBuildContext, ArgumentType<T>> argumentType,
	BiFunction<CommandContext<?>, String, T> argumentGetter
) {
	public static final Map<ResourceLocation, RegisteredDataType<?>> REGISTRY = new Object2ObjectOpenHashMap<>();

	public static <T> RegisteredDataType<T> register(
		ResourceLocation id,
		DataType<T> type,
		BiFunction<RegisteredDataType<T>, CommandBuildContext, ArgumentType<T>> argumentType,
		BiFunction<CommandContext<?>, String, T> argumentGetter
	) {
		var old = REGISTRY.get(id);

		if (old != null) {
			throw new IllegalStateException("Duplicate registered data type: " + id);
		}

		var t = new RegisteredDataType<>(id, type, argumentType, argumentGetter);
		REGISTRY.put(id, t);
		return t;
	}

	private static ArgumentType enumArgument(Class<?> cl) {
		return EnumArgument.enumArgument((Class) cl);
	}

	public static <T> RegisteredDataType<T> register(ResourceLocation id, DataType<T> dataType) {
		return register(
			id,
			dataType,
			(self, ctx) -> {
				if (dataType.typeClass().isEnum()) {
					return enumArgument(dataType.typeClass());
				} else {
					var ops = ctx.createSerializationContext(NbtOps.INSTANCE);
					return new RegisteredDataTypeArgument<>(ops, TagParser.create(ops), self);
				}
			},
			(ctx, name) -> ctx.getArgument(name, dataType.typeClass())
		);
	}

	public static <T> RegisteredDataType<T> of(VLRegistry<T> registry, Class<T> type) {
		return register(
			registry.id,
			DataType.of(registry, type),
			registry,
			(ctx, name) -> ctx.getArgument(name, type)
		);
	}

	public static <T> RegisteredDataType<ResourceKey<T>> register(ResourceKey<? extends Registry<T>> registry) {
		return register(
			registry.location(),
			DataType.of(registry),
			(self, ctx) -> ResourceKeyArgument.key(registry),
			(ctx, name) -> Cast.to(ctx.getArgument(name, ResourceKey.class))
		);
	}

	public static final RegisteredDataType<Boolean> BOOL = register(ResourceLocation.fromNamespaceAndPath("java", "bool"), DataType.BOOL, (self, ctx) -> BoolArgumentType.bool(), BoolArgumentType::getBool);
	public static final RegisteredDataType<Integer> INT = register(ResourceLocation.fromNamespaceAndPath("java", "int"), DataType.INT, (self, ctx) -> IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
	public static final RegisteredDataType<Integer> VAR_INT = register(ResourceLocation.fromNamespaceAndPath("java", "var_int"), DataType.VAR_INT, (self, ctx) -> IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
	public static final RegisteredDataType<Long> LONG = register(ResourceLocation.fromNamespaceAndPath("java", "long"), DataType.LONG, (self, ctx) -> LongArgumentType.longArg(), LongArgumentType::getLong);
	public static final RegisteredDataType<Long> VAR_LONG = register(ResourceLocation.fromNamespaceAndPath("java", "var_long"), DataType.VAR_LONG, (self, ctx) -> LongArgumentType.longArg(), LongArgumentType::getLong);
	public static final RegisteredDataType<Float> FLOAT = register(ResourceLocation.fromNamespaceAndPath("java", "float"), DataType.FLOAT, (self, ctx) -> FloatArgumentType.floatArg(), FloatArgumentType::getFloat);
	public static final RegisteredDataType<Double> DOUBLE = register(ResourceLocation.fromNamespaceAndPath("java", "double"), DataType.DOUBLE, (self, ctx) -> DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble);
	public static final RegisteredDataType<String> STRING = register(ResourceLocation.fromNamespaceAndPath("java", "string"), DataType.STRING, (self, ctx) -> StringArgumentType.string(), StringArgumentType::getString);
	public static final RegisteredDataType<UUID> UUID = register(ResourceLocation.fromNamespaceAndPath("java", "uuid"), DataType.UUID, (self, ctx) -> UuidArgument.uuid(), (ctx, name) -> ctx.getArgument(name, UUID.class));

	public static final RegisteredDataType<ResourceLocation> ID = register(ResourceLocation.withDefaultNamespace("id"), DataType.ID, (self, ctx) -> ResourceLocationArgument.id(), (ctx, name) -> ctx.getArgument(name, ResourceLocation.class));
	public static final RegisteredDataType<Component> TEXT_COMPONENT = register(ResourceLocation.withDefaultNamespace("text_component"), DataType.TEXT_COMPONENT, (self, ctx) -> ComponentArgument.textComponent(ctx), (ctx, name) -> ctx.getArgument(name, Component.class));
	public static final RegisteredDataType<Mirror> MIRROR = register(ResourceLocation.withDefaultNamespace("mirror"), DataType.MIRROR);
	public static final RegisteredDataType<Rotation> BLOCK_ROTATION = register(ResourceLocation.withDefaultNamespace("rotation"), DataType.BLOCK_ROTATION);
	public static final RegisteredDataType<LiquidSettings> LIQUID_SETTINGS = register(ResourceLocation.withDefaultNamespace("liquid_settings"), DataType.LIQUID_SETTINGS);
	public static final RegisteredDataType<InteractionHand> HAND = register(ResourceLocation.withDefaultNamespace("hand"), DataType.HAND);
	public static final RegisteredDataType<SoundSource> SOUND_SOURCE = register(ResourceLocation.withDefaultNamespace("sound_source"), DataType.SOUND_SOURCE);
	public static final RegisteredDataType<ItemStack> ITEM_STACK = register(ResourceLocation.withDefaultNamespace("item_stack"), DataType.ITEM_STACK);
	public static final RegisteredDataType<ParticleOptions> PARTICLE_OPTIONS = register(ResourceLocation.withDefaultNamespace("particle_options"), DataType.PARTICLE_OPTIONS);
	public static final RegisteredDataType<BlockState> BLOCK_STATE = register(ResourceLocation.withDefaultNamespace("block_state"), DataType.BLOCK_STATE);
	public static final RegisteredDataType<FluidState> FLUID_STATE = register(ResourceLocation.withDefaultNamespace("fluid_state"), DataType.FLUID_STATE);

	public static final RegisteredDataType<Color> COLOR = register(VidLib.id("color"), DataType.COLOR);
	public static final RegisteredDataType<Gradient> GRADIENT = register(VidLib.id("gradient"), DataType.GRADIENT);
	public static final RegisteredDataType<Shape> SHAPE = register(VidLib.id("shape"), DataType.SHAPE);
	public static final RegisteredDataType<dev.latvian.mods.kmath.Rotation> ROTATION = register(VidLib.id("rotation"), DataType.ROTATION);
	public static final RegisteredDataType<MovementType> MOVEMENT_TYPE = register(VidLib.id("movement_type"), DataType.MOVEMENT_TYPE);
	public static final RegisteredDataType<Range> RANGE = register(VidLib.id("range"), DataType.RANGE);

	public ArgumentType<T> argument(CommandBuildContext commandBuildContext) {
		return argumentType.apply(this, commandBuildContext);
	}

	public T get(CommandContext<?> ctx, String name) {
		return argumentGetter.apply(ctx, name);
	}

	public RegisteredDataType<List<T>> listOf() {
		return register(id.withPath(p -> p + "_list"), type.listOf());
	}

	public RegisteredDataType<Set<T>> setOf() {
		return register(id.withPath(p -> p + "_set"), type.setOf());
	}
}
