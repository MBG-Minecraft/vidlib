package dev.latvian.mods.vidlib.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.Range;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FloatConfigValue<T> extends ConfigValue<T, Float> {
	public final Range range;
	public final boolean slider;

	public FloatConfigValue(String name, @Nullable Range range, boolean slider, Function<T, Float> getter, BiConsumer<T, Float> setter) {
		super(name, Codec.FLOAT, getter, setter);
		this.range = range;
		this.slider = slider;
	}

	@Override
	public <O> Component valueComponent(DynamicOps<O> ops, Float value) {
		if (range == Range.FULL) {
			return Component.literal(KMath.format(value * 100F) + "%").withStyle(ChatFormatting.GOLD);
		}

		return Component.literal(KMath.format(value)).withStyle(ChatFormatting.GOLD);
	}
}
