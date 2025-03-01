package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

public record ScaledWorldNumber(WorldNumber a, WorldNumber b) implements WorldNumber {
	public static final SimpleRegistryType<ScaledWorldNumber> TYPE = SimpleRegistryType.dynamic(Shimmer.id("offset"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("a").forGetter(ScaledWorldNumber::a),
		WorldNumber.CODEC.fieldOf("b").forGetter(ScaledWorldNumber::b)
	).apply(instance, ScaledWorldNumber::new)), StreamCodec.composite(
		WorldNumber.STREAM_CODEC,
		ScaledWorldNumber::a,
		WorldNumber.STREAM_CODEC,
		ScaledWorldNumber::b,
		ScaledWorldNumber::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double get(Level level, float progress) {
		var a = this.a.get(level, progress);
		var b = this.b.get(level, progress);
		return a * b;
	}
}
