package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;

public record FixedWorldNumber(double number) implements WorldNumber {
	public static final SimpleRegistryType.Unit<FixedWorldNumber> ZERO = SimpleRegistryType.unit(Shimmer.id("zero"), new FixedWorldNumber(0D));
	public static final SimpleRegistryType.Unit<FixedWorldNumber> ONE = SimpleRegistryType.unit(Shimmer.id("one"), new FixedWorldNumber(1D));

	public static FixedWorldNumber of(double number) {
		if (number == 0D) {
			return ZERO.instance();
		} else if (number == 1D) {
			return ONE.instance();
		} else {
			return new FixedWorldNumber(number);
		}
	}

	public static final SimpleRegistryType<FixedWorldNumber> TYPE = SimpleRegistryType.dynamic(Shimmer.id("fixed"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedWorldNumber::number)
	).apply(instance, FixedWorldNumber::of)), ByteBufCodecs.DOUBLE.map(FixedWorldNumber::of, FixedWorldNumber::number));

	@Override
	public SimpleRegistryType<?> type() {
		return number == 0D ? ZERO : number == 1D ? ONE : TYPE;
	}

	@Override
	public double get(Level level, float progress) {
		return number;
	}
}
