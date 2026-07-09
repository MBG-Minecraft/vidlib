package dev.latvian.mods.vidlib.feature.entity.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

public record FixedEntityNumber(double number) implements EntityNumber {
	public static final CustomRegistryType<RegistryFriendlyByteBuf, EntityNumber> TYPE = REGISTRY.dynamic(ID.vidlib("fixed"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedEntityNumber::number)
	).apply(instance, EntityNumber::of)), ByteBufCodecs.DOUBLE.map(EntityNumber::of, FixedEntityNumber::number));

	public static final FixedEntityNumber ZERO = new FixedEntityNumber(0D);
	public static final FixedEntityNumber ONE = new FixedEntityNumber(1D);

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, EntityNumber> type() {
		return TYPE;
	}

	@Override
	public double applyAsDouble(Entity entity) {
		return number;
	}
}
