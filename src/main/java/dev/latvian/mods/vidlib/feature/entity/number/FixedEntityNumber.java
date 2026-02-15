package dev.latvian.mods.vidlib.feature.entity.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

public record FixedEntityNumber(double number) implements EntityNumber {
	public static final SimpleRegistryType<FixedEntityNumber> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedEntityNumber::number)
	).apply(instance, EntityNumber::of)), ByteBufCodecs.DOUBLE.map(EntityNumber::of, FixedEntityNumber::number));

	public static final FixedEntityNumber ZERO = new FixedEntityNumber(0D);
	public static final FixedEntityNumber ONE = new FixedEntityNumber(1D);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double applyAsDouble(Entity entity) {
		return number;
	}
}
