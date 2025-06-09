package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;

public record FixedWorldNumber(Double number) implements WorldNumber {
	public static final SimpleRegistryType.Unit<FixedWorldNumber> ZERO = SimpleRegistryType.unit("zero", new FixedWorldNumber(0D));
	public static final SimpleRegistryType.Unit<FixedWorldNumber> ONE = SimpleRegistryType.unit("one", new FixedWorldNumber(1D));

	public static FixedWorldNumber of(double number) {
		if (number == 0D) {
			return ZERO.instance();
		} else if (number == 1D) {
			return ONE.instance();
		} else {
			return new FixedWorldNumber(number);
		}
	}

	public static final SimpleRegistryType<FixedWorldNumber> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedWorldNumber::number)
	).apply(instance, FixedWorldNumber::of)), ByteBufCodecs.DOUBLE.map(FixedWorldNumber::of, FixedWorldNumber::number));

	@Override
	public SimpleRegistryType<?> type() {
		return number == 0D ? ZERO : number == 1D ? ONE : TYPE;
	}

	@Override
	public Double get(WorldNumberContext ctx) {
		return number;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
