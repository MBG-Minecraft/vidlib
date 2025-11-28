package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.function.Function;

@AutoInit
public record Clothing(ResourceLocation id, ClothingParts parts) {
	public static final Clothing NONE = new Clothing(VidLib.id("none"), ClothingParts.NONE);

	public static final Codec<Clothing> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(Clothing::id),
		ClothingParts.CODEC.optionalFieldOf("parts", ClothingParts.ALL).forGetter(Clothing::parts)
	).apply(instance, Clothing::new));

	public static final Codec<Clothing> FULL_CODEC = Codec.either(ResourceLocation.CODEC, MAP_CODEC).xmap(either -> either.map(Clothing::new, Function.identity()), c -> c.parts.equals(ClothingParts.ALL) ? Either.left(c.id) : Either.right(c));

	public static final Codec<Clothing> CODEC = Codec.either(Codec.BOOL, FULL_CODEC).xmap(either -> either.map(b -> b ? Tracksuits.BLUE : NONE, Function.identity()), c -> c.equals(NONE) ? Either.left(false) : c.equals(Tracksuits.BLUE) ? Either.left(true) : Either.right(c));

	public static final StreamCodec<ByteBuf, Clothing> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, Clothing::id,
		ClothingParts.STREAM_CODEC, Clothing::parts,
		Clothing::new
	);

	public static final DataType<Clothing> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Clothing.class);

	public static final EquipmentSlot[] ORDERED_SLOTS = {
		EquipmentSlot.LEGS,
		EquipmentSlot.CHEST,
		EquipmentSlot.FEET,
		EquipmentSlot.HEAD
	};

	public Clothing(ResourceLocation id) {
		this(id, ClothingParts.ALL);
	}

	public Clothing withParts(ClothingParts parts) {
		return new Clothing(id, parts);
	}

	@Override
	public String toString() {
		if (parts.equals(ClothingParts.ALL)) {
			return "Clothing[" + id + "]";
		} else {
			return "Clothing[" + id + "," + parts + "]";
		}
	}
}
