package dev.beast.mods.shimmer.feature.clothing;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@AutoInit
public record Clothing(ResourceKey<EquipmentAsset> id, ClothingParts parts) {
	public static final List<ResourceLocation> CLOTHING_IDS = new ArrayList<>();
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ShimmerResourceLocationArgument.registerSuggestionProvider(Shimmer.id("clothing"), () -> CLOTHING_IDS);

	public static ResourceKey<EquipmentAsset> createKey(ResourceLocation id) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, id);
	}

	public static ResourceKey<EquipmentAsset> internal(String key) {
		return createKey(Shimmer.id(key));
	}

	public static ResourceKey<EquipmentAsset> video(String key) {
		return createKey(ResourceLocation.fromNamespaceAndPath("video", key));
	}

	public static final Clothing NONE = new Clothing(internal("none"), ClothingParts.NONE);
	public static final Clothing HOST = new Clothing(internal("host"));
	public static final Clothing HOST_WITH_MASK = new Clothing(internal("host_with_mask"));

	public static final Clothing BLACK_TRACKSUIT = new Clothing(internal("tracksuit/black"));
	public static final Clothing WHITE_TRACKSUIT = new Clothing(internal("tracksuit/white"));
	public static final Clothing RED_TRACKSUIT = new Clothing(internal("tracksuit/red"));
	public static final Clothing PINK_TRACKSUIT = new Clothing(internal("tracksuit/pink"));
	public static final Clothing MAGENTA_TRACKSUIT = new Clothing(internal("tracksuit/magenta"));
	public static final Clothing PURPLE_TRACKSUIT = new Clothing(internal("tracksuit/purple"));
	public static final Clothing BLUE_TRACKSUIT = new Clothing(internal("tracksuit/blue"));
	public static final Clothing CYAN_TRACKSUIT = new Clothing(internal("tracksuit/cyan"));
	public static final Clothing GREEN_TRACKSUIT = new Clothing(internal("tracksuit/green"));
	public static final Clothing LIME_TRACKSUIT = new Clothing(internal("tracksuit/lime"));
	public static final Clothing YELLOW_TRACKSUIT = new Clothing(internal("tracksuit/yellow"));
	public static final Clothing ORANGE_TRACKSUIT = new Clothing(internal("tracksuit/orange"));

	public static final List<Clothing> COLORED_TRACKSUITS = List.of(
		RED_TRACKSUIT,
		PINK_TRACKSUIT,
		MAGENTA_TRACKSUIT,
		PURPLE_TRACKSUIT,
		BLUE_TRACKSUIT,
		CYAN_TRACKSUIT,
		GREEN_TRACKSUIT,
		LIME_TRACKSUIT,
		YELLOW_TRACKSUIT,
		ORANGE_TRACKSUIT
	);

	public static final Clothing SQUID_TRACKSUIT = new Clothing(internal("tracksuit/squid"));

	public static final Codec<Clothing> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceKey.codec(EquipmentAssets.ROOT_ID).fieldOf("id").forGetter(Clothing::id),
		ClothingParts.CODEC.optionalFieldOf("parts", ClothingParts.ALL).forGetter(Clothing::parts)
	).apply(instance, Clothing::new));

	public static final Codec<Clothing> FULL_CODEC = Codec.either(ResourceKey.codec(EquipmentAssets.ROOT_ID), MAP_CODEC).xmap(either -> either.map(Clothing::new, Function.identity()), c -> c.parts.equals(ClothingParts.ALL) ? Either.left(c.id) : Either.right(c));

	public static final Codec<Clothing> CODEC = Codec.either(Codec.BOOL, FULL_CODEC).xmap(either -> either.map(b -> b ? BLUE_TRACKSUIT : NONE, Function.identity()), c -> c.equals(NONE) ? Either.left(false) : c.equals(BLUE_TRACKSUIT) ? Either.left(true) : Either.right(c));

	public static final StreamCodec<ByteBuf, Clothing> STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.resourceKey(EquipmentAssets.ROOT_ID), Clothing::id,
		ClothingParts.STREAM_CODEC, Clothing::parts,
		Clothing::new
	);

	public static final KnownCodec<Clothing> KNOWN_CODEC = KnownCodec.register(Shimmer.id("clothing"), CODEC, STREAM_CODEC, Clothing.class);

	public static final EquipmentSlot[] ORDERED_SLOTS = {
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.HEAD
	};

	public Clothing(ResourceKey<EquipmentAsset> id) {
		this(id, ClothingParts.ALL);
	}

	public Clothing withParts(ClothingParts parts) {
		return new Clothing(id, parts);
	}

	@Override
	public String toString() {
		if (parts.equals(ClothingParts.ALL)) {
			return "Clothing[" + id.location() + "]";
		} else {
			return "Clothing[" + id.location() + "," + parts + "]";
		}
	}
}
