package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.MovementType;
import dev.latvian.mods.kmath.Range;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public record DataType<T>(
	Codec<T> codec,
	Codec<Optional<T>> optionalCodec,
	StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
	Class<T> typeClass
) {
	public static <T> DataType<T> of(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Class<T> typeClass) {
		return new DataType<>(codec, VLCodecs.optional(codec), streamCodec, typeClass);
	}

	public static <E extends Enum<E>> DataType<E> of(E[] values, Function<E, String> nameGetter) {
		return of(
			VLCodecs.anyEnumCodec(values, nameGetter),
			VLStreamCodecs.enumValue(values),
			Cast.to(values.getClass().getComponentType())
		);
	}

	public static <E extends Enum<E>> DataType<E> of(E[] values) {
		return of(values, (Function<E, String>) VLCodecs.DEFAULT_NAME_GETTER);
	}

	public static <T> DataType<T> of(VLRegistry<T> registry, Class<T> typeClass) {
		return of(
			VLCodecs.map(registry::getMap, dev.latvian.mods.vidlib.feature.registry.ID.CODEC, registry::getId),
			VLStreamCodecs.map(registry::getMap, dev.latvian.mods.vidlib.feature.registry.ID.STREAM_CODEC, registry::getId),
			typeClass
		);
	}

	public static <T> DataType<ResourceKey<T>> of(ResourceKey<? extends Registry<T>> registry) {
		return of(
			ResourceKey.codec(registry),
			VLStreamCodecs.resourceKey(registry),
			Cast.to(ResourceKey.class)
		);
	}

	public static final DataType<Boolean> BOOL = of(Codec.BOOL, ByteBufCodecs.BOOL, Boolean.class);
	public static final DataType<Integer> INT = of(Codec.INT, ByteBufCodecs.INT, Integer.class);
	public static final DataType<Integer> VAR_INT = of(Codec.INT, ByteBufCodecs.VAR_INT, Integer.class);
	public static final DataType<Long> LONG = of(Codec.LONG, ByteBufCodecs.LONG, Long.class);
	public static final DataType<Long> VAR_LONG = of(Codec.LONG, ByteBufCodecs.VAR_LONG, Long.class);
	public static final DataType<Float> FLOAT = of(Codec.FLOAT, ByteBufCodecs.FLOAT, Float.class);
	public static final DataType<Double> DOUBLE = of(Codec.DOUBLE, ByteBufCodecs.DOUBLE, Double.class);
	public static final DataType<String> STRING = of(Codec.STRING, ByteBufCodecs.STRING_UTF8, String.class);
	public static final DataType<UUID> UUID = of(VLCodecs.UUID, VLStreamCodecs.UUID, UUID.class);

	public static final DataType<ResourceLocation> ID = of(dev.latvian.mods.vidlib.feature.registry.ID.CODEC, ResourceLocation.STREAM_CODEC, ResourceLocation.class);
	public static final DataType<Component> TEXT_COMPONENT = of(ComponentSerialization.CODEC, ComponentSerialization.STREAM_CODEC, Component.class);
	public static final DataType<Mirror> MIRROR = of(Mirror.values());
	public static final DataType<Rotation> BLOCK_ROTATION = of(Rotation.values());
	public static final DataType<LiquidSettings> LIQUID_SETTINGS = of(LiquidSettings.values());
	public static final DataType<InteractionHand> HAND = of(InteractionHand.values());
	public static final DataType<SoundSource> SOUND_SOURCE = of(SoundSource.values());
	public static final DataType<ItemStack> ITEM_STACK = of(ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC, ItemStack.class);
	public static final DataType<ParticleOptions> PARTICLE_OPTIONS = of(ParticleTypes.CODEC, ParticleTypes.STREAM_CODEC, ParticleOptions.class);

	public static final DataType<Color> COLOR = of(Color.CODEC, Color.STREAM_CODEC, Color.class);
	public static final DataType<Gradient> GRADIENT = of(Gradient.CODEC, Gradient.STREAM_CODEC, Gradient.class);
	public static final DataType<dev.latvian.mods.kmath.Rotation> ROTATION = of(dev.latvian.mods.kmath.Rotation.CODEC, dev.latvian.mods.kmath.Rotation.STREAM_CODEC, dev.latvian.mods.kmath.Rotation.class);
	public static final DataType<MovementType> MOVEMENT_TYPE = of(MovementType.values());
	public static final DataType<Range> RANGE = of(Range.CODEC, Range.STREAM_CODEC, Range.class);

	public DataType<List<T>> listOf() {
		return of(codec.listOf(), streamCodec.listOf(), Cast.to(List.class));
	}

	public DataType<Set<T>> setOf() {
		return of(VLCodecs.setOf(codec), streamCodec.setOf(), Cast.to(Set.class));
	}
}
