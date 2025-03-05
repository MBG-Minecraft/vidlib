package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;

public record VariableWorldNumber(String name) implements WorldNumber {
	public static final SimpleRegistryType<VariableWorldNumber> TYPE = SimpleRegistryType.dynamic(Shimmer.id("variable"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableWorldNumber::name)
	).apply(instance, VariableWorldNumber::new)), ByteBufCodecs.STRING_UTF8.map(VariableWorldNumber::new, VariableWorldNumber::name));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double get(WorldNumberContext ctx) {
		return ctx.variables.numbers().get(name).get(ctx);
	}
}
