package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.List;
import java.util.function.Function;

@AutoInit
public record Clothing(ResourceKey<EquipmentAsset> id, ClothingParts parts) {
	public static ResourceKey<EquipmentAsset> createKey(String id) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, ID.mc(id));
	}

	public static final Clothing NONE = new Clothing(createKey("none"), ClothingParts.NONE);

	public static final Codec<Clothing> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceKey.codec(EquipmentAssets.ROOT_ID).fieldOf("id").forGetter(Clothing::id),
		ClothingParts.CODEC.optionalFieldOf("parts", ClothingParts.ALL).forGetter(Clothing::parts)
	).apply(instance, Clothing::new));

	public static final KnownCodec<Clothing> KNOWN_CODEC = KnownCodec.register(VidLib.id("clothing"), Codec.either(ResourceKey.codec(EquipmentAssets.ROOT_ID), MAP_CODEC).xmap(either -> either.map(Clothing::new, Function.identity()), c -> c.parts.equals(ClothingParts.ALL) ? Either.left(c.id) : Either.right(c)), CompositeStreamCodec.of(
		VLStreamCodecs.resourceKey(EquipmentAssets.ROOT_ID), Clothing::id,
		ClothingParts.STREAM_CODEC, Clothing::parts,
		Clothing::new), Clothing.class);

	public static final EquipmentSlot[] ORDERED_SLOTS = {
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.HEAD
	};

	public static final ResourceKey<EquipmentAsset> HOST = createKey("host");
	public static final ResourceKey<EquipmentAsset> HOST_WITH_MASK = createKey("host_with_mask");

	public static final ResourceKey<EquipmentAsset> BLACK_TRACKSUIT = createKey("tracksuit/black");
	public static final ResourceKey<EquipmentAsset> WHITE_TRACKSUIT = createKey("tracksuit/white");
	public static final ResourceKey<EquipmentAsset> RED_TRACKSUIT = createKey("tracksuit/red");
	public static final ResourceKey<EquipmentAsset> PINK_TRACKSUIT = createKey("tracksuit/pink");
	public static final ResourceKey<EquipmentAsset> MAGENTA_TRACKSUIT = createKey("tracksuit/magenta");
	public static final ResourceKey<EquipmentAsset> PURPLE_TRACKSUIT = createKey("tracksuit/purple");
	public static final ResourceKey<EquipmentAsset> BLUE_TRACKSUIT = createKey("tracksuit/blue");
	public static final ResourceKey<EquipmentAsset> CYAN_TRACKSUIT = createKey("tracksuit/cyan");
	public static final ResourceKey<EquipmentAsset> GREEN_TRACKSUIT = createKey("tracksuit/green");
	public static final ResourceKey<EquipmentAsset> LIME_TRACKSUIT = createKey("tracksuit/lime");
	public static final ResourceKey<EquipmentAsset> YELLOW_TRACKSUIT = createKey("tracksuit/yellow");
	public static final ResourceKey<EquipmentAsset> ORANGE_TRACKSUIT = createKey("tracksuit/orange");

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

	public static final ResourceKey<EquipmentAsset> SQUID_TRACKSUIT = createKey("tracksuit/squid");

	public Clothing(ResourceKey<EquipmentAsset> id) {
		this(id, ClothingParts.ALL);
	}
}
