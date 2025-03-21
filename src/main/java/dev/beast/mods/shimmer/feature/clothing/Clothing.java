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
import net.minecraft.commands.CommandSourceStack;
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

	public static final Codec<Clothing> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceKey.codec(EquipmentAssets.ROOT_ID).fieldOf("id").forGetter(Clothing::id),
		ClothingParts.CODEC.optionalFieldOf("parts", ClothingParts.ALL).forGetter(Clothing::parts)
	).apply(instance, Clothing::new));

	public static final KnownCodec<Clothing> KNOWN_CODEC = KnownCodec.register(Shimmer.id("clothing"), Codec.either(ResourceKey.codec(EquipmentAssets.ROOT_ID), MAP_CODEC).xmap(either -> either.map(Clothing::new, Function.identity()), c -> c.parts.equals(ClothingParts.ALL) ? Either.left(c.id) : Either.right(c)), CompositeStreamCodec.of(
		ShimmerStreamCodecs.resourceKey(EquipmentAssets.ROOT_ID), Clothing::id,
		ClothingParts.STREAM_CODEC, Clothing::parts,
		Clothing::new), Clothing.class);

	public static final EquipmentSlot[] ORDERED_SLOTS = {
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.HEAD
	};

	public static final ResourceKey<EquipmentAsset> HOST = internal("host");
	public static final ResourceKey<EquipmentAsset> HOST_WITH_MASK = internal("host_with_mask");

	public static final ResourceKey<EquipmentAsset> BLACK_TRACKSUIT = internal("tracksuit/black");
	public static final ResourceKey<EquipmentAsset> WHITE_TRACKSUIT = internal("tracksuit/white");
	public static final ResourceKey<EquipmentAsset> RED_TRACKSUIT = internal("tracksuit/red");
	public static final ResourceKey<EquipmentAsset> PINK_TRACKSUIT = internal("tracksuit/pink");
	public static final ResourceKey<EquipmentAsset> MAGENTA_TRACKSUIT = internal("tracksuit/magenta");
	public static final ResourceKey<EquipmentAsset> PURPLE_TRACKSUIT = internal("tracksuit/purple");
	public static final ResourceKey<EquipmentAsset> BLUE_TRACKSUIT = internal("tracksuit/blue");
	public static final ResourceKey<EquipmentAsset> CYAN_TRACKSUIT = internal("tracksuit/cyan");
	public static final ResourceKey<EquipmentAsset> GREEN_TRACKSUIT = internal("tracksuit/green");
	public static final ResourceKey<EquipmentAsset> LIME_TRACKSUIT = internal("tracksuit/lime");
	public static final ResourceKey<EquipmentAsset> YELLOW_TRACKSUIT = internal("tracksuit/yellow");
	public static final ResourceKey<EquipmentAsset> ORANGE_TRACKSUIT = internal("tracksuit/orange");

	public static final List<ResourceKey<EquipmentAsset>> COLORED_TRACKSUITS = List.of(
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

	public static final ResourceKey<EquipmentAsset> SQUID_TRACKSUIT = internal("tracksuit/squid");

	public Clothing(ResourceKey<EquipmentAsset> id) {
		this(id, ClothingParts.ALL);
	}
}
