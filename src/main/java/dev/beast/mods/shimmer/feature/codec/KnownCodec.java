package dev.beast.mods.shimmer.feature.codec;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.registry.ShimmerRegistry;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import dev.beast.mods.shimmer.util.registry.VideoResourceLocationArgument;
import dev.latvian.mods.kmath.MovementType;
import dev.latvian.mods.kmath.Range;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

@AutoInit
public record KnownCodec<T>(
	ResourceLocation id,
	Codec<T> codec,
	Codec<Optional<T>> optionalCodec,
	StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
	BiFunction<KnownCodec<T>, CommandBuildContext, ArgumentType<T>> argumentType,
	BiFunction<CommandContext<?>, String, T> argumentGetter
) {
	public static final Map<ResourceLocation, KnownCodec<?>> MAP = new HashMap<>();

	public static <T> KnownCodec<T> register(ResourceLocation id, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, BiFunction<KnownCodec<T>, CommandBuildContext, ArgumentType<T>> command, BiFunction<CommandContext<?>, String, T> argumentGetter) {
		var old = MAP.get(id);

		if (old != null) {
			return (KnownCodec<T>) old;
		}

		var knownCodec = new KnownCodec<>(id, codec, ShimmerCodecs.optional(codec), streamCodec, command, argumentGetter);
		MAP.put(id, knownCodec);
		return knownCodec;
	}

	public static <T> KnownCodec<T> register(ResourceLocation id, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Class<T> type) {
		return register(
			id,
			codec,
			streamCodec,
			(self, ctx) -> new CodecArgument<>(ctx.createSerializationContext(NbtOps.INSTANCE), self),
			(ctx, name) -> ctx.getArgument(name, type)
		);
	}

	public static <E extends Enum<E>> KnownCodec<E> registerEnum(ResourceLocation id, E[] values, Function<E, String> nameGetter) {
		Class<E> enumClass = (Class<E>) values.getClass().getComponentType();

		return register(
			id,
			ShimmerCodecs.anyEnumCodec(values, nameGetter),
			ShimmerStreamCodecs.enumValue(values),
			(self, ctx) -> EnumArgument.enumArgument(enumClass),
			(ctx, name) -> ctx.getArgument(name, enumClass)
		);
	}

	public static <E extends Enum<E>> KnownCodec<E> registerEnum(ResourceLocation id, E[] values) {
		return registerEnum(id, values, (Function<E, String>) ShimmerCodecs.DEFAULT_NAME_GETTER);
	}

	public static <T> KnownCodec<T> of(ShimmerRegistry<T> registry, Class<T> type) {
		return register(
			registry.id,
			ShimmerCodecs.map(registry::getMap, registry.keyCodec, registry::getId),
			ShimmerStreamCodecs.map(registry::getMap, registry.keyStreamCodec, registry::getId),
			registry,
			(ctx, name) -> ctx.getArgument(name, type)
		);
	}

	public static <T> KnownCodec<ResourceKey<T>> register(ResourceKey<? extends Registry<T>> registry) {
		return register(
			registry.location(),
			ResourceKey.codec(registry),
			ShimmerStreamCodecs.resourceKey(registry),
			(self, ctx) -> ResourceKeyArgument.key(registry),
			(ctx, name) -> Cast.to(ctx.getArgument(name, ResourceKey.class))
		);
	}

	public static final KnownCodec<Boolean> BOOL = register(ResourceLocation.fromNamespaceAndPath("java", "bool"), Codec.BOOL, ByteBufCodecs.BOOL, (self, ctx) -> BoolArgumentType.bool(), BoolArgumentType::getBool);
	public static final KnownCodec<Integer> INT = register(ResourceLocation.fromNamespaceAndPath("java", "int"), Codec.INT, ByteBufCodecs.INT, (self, ctx) -> IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
	public static final KnownCodec<Integer> VAR_INT = register(ResourceLocation.fromNamespaceAndPath("java", "var_int"), Codec.INT, ByteBufCodecs.VAR_INT, (self, ctx) -> IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
	public static final KnownCodec<Long> LONG = register(ResourceLocation.fromNamespaceAndPath("java", "long"), Codec.LONG, ByteBufCodecs.LONG, (self, ctx) -> LongArgumentType.longArg(), LongArgumentType::getLong);
	public static final KnownCodec<Long> VAR_LONG = register(ResourceLocation.fromNamespaceAndPath("java", "var_long"), Codec.LONG, ByteBufCodecs.VAR_LONG, (self, ctx) -> LongArgumentType.longArg(), LongArgumentType::getLong);
	public static final KnownCodec<Float> FLOAT = register(ResourceLocation.fromNamespaceAndPath("java", "float"), Codec.FLOAT, ByteBufCodecs.FLOAT, (self, ctx) -> FloatArgumentType.floatArg(), FloatArgumentType::getFloat);
	public static final KnownCodec<Double> DOUBLE = register(ResourceLocation.fromNamespaceAndPath("java", "double"), Codec.DOUBLE, ByteBufCodecs.DOUBLE, (self, ctx) -> DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble);
	public static final KnownCodec<String> STRING = register(ResourceLocation.fromNamespaceAndPath("java", "string"), Codec.STRING, ByteBufCodecs.STRING_UTF8, (self, ctx) -> StringArgumentType.string(), StringArgumentType::getString);
	public static final KnownCodec<UUID> UUID = register(ResourceLocation.fromNamespaceAndPath("java", "uuid"), ShimmerCodecs.UUID, ShimmerStreamCodecs.UUID, (self, ctx) -> UuidArgument.uuid(), (ctx, name) -> ctx.getArgument(name, UUID.class));

	public static final KnownCodec<ResourceLocation> ID = register(ResourceLocation.withDefaultNamespace("id"), ResourceLocation.CODEC, ResourceLocation.STREAM_CODEC, (self, ctx) -> ResourceLocationArgument.id(), (ctx, name) -> ctx.getArgument(name, ResourceLocation.class));
	public static final KnownCodec<Component> TEXT_COMPONENT = register(ResourceLocation.withDefaultNamespace("text_component"), ComponentSerialization.CODEC, ComponentSerialization.STREAM_CODEC, (self, ctx) -> ComponentArgument.textComponent(ctx), (ctx, name) -> ctx.getArgument(name, Component.class));
	public static final KnownCodec<Mirror> MIRROR = registerEnum(ResourceLocation.withDefaultNamespace("mirror"), Mirror.values());
	public static final KnownCodec<Rotation> BLOCK_ROTATION = registerEnum(ResourceLocation.withDefaultNamespace("rotation"), Rotation.values());
	public static final KnownCodec<LiquidSettings> LIQUID_SETTINGS = registerEnum(ResourceLocation.withDefaultNamespace("liquid_settings"), LiquidSettings.values());
	public static final KnownCodec<InteractionHand> HAND = registerEnum(ResourceLocation.withDefaultNamespace("hand"), InteractionHand.values());
	public static final KnownCodec<SoundSource> SOUND_SOURCE = registerEnum(ResourceLocation.withDefaultNamespace("sound_source"), SoundSource.values());
	public static final KnownCodec<ItemStack> OPTIONAL_ITEM = register(ResourceLocation.withDefaultNamespace("optional_item"), ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC, ItemStack.class);
	public static final KnownCodec<ParticleOptions> PARTICLE_OPTIONS = register(ResourceLocation.withDefaultNamespace("particle_options"), ParticleTypes.CODEC, ParticleTypes.STREAM_CODEC, ParticleOptions.class);

	public static final KnownCodec<ResourceLocation> SHIMMER_ID = register(Shimmer.id("shimmer_id"), ShimmerCodecs.SHIMMER_ID, ShimmerStreamCodecs.SHIMMER_ID, (self, ctx) -> ShimmerResourceLocationArgument.id(), ShimmerResourceLocationArgument::getId);
	public static final KnownCodec<ResourceLocation> VIDEO_ID = register(Shimmer.id("video_id"), ShimmerCodecs.VIDEO_ID, ShimmerStreamCodecs.VIDEO_ID, (self, ctx) -> VideoResourceLocationArgument.id(), VideoResourceLocationArgument::getId);

	public static final KnownCodec<Color> COLOR = register(Shimmer.id("color"), Color.CODEC, Color.STREAM_CODEC, Color.class);
	public static final KnownCodec<dev.latvian.mods.kmath.Rotation> ROTATION = register(Shimmer.id("rotation"), dev.latvian.mods.kmath.Rotation.CODEC, dev.latvian.mods.kmath.Rotation.STREAM_CODEC, dev.latvian.mods.kmath.Rotation.class);
	public static final KnownCodec<MovementType> MOVEMENT_TYPE = registerEnum(Shimmer.id("movement_type"), MovementType.values());
	public static final KnownCodec<Range> RANGE = register(Shimmer.id("range"), Range.CODEC, Range.STREAM_CODEC, Range.class);

	public ArgumentType<T> argument(CommandBuildContext commandBuildContext) {
		return argumentType.apply(this, commandBuildContext);
	}

	public T get(CommandContext<?> ctx, String name) {
		return argumentGetter.apply(ctx, name);
	}

	public KnownCodec<List<T>> listOf() {
		return register(id.withPath(p -> p + "_list"), codec.listOf(), streamCodec.list(), (Class) List.class);
	}

	public KnownCodec<Set<T>> setOf() {
		return register(id.withPath(p -> p + "_set"), ShimmerCodecs.set(codec), streamCodec.set(), (Class) Set.class);
	}
}
