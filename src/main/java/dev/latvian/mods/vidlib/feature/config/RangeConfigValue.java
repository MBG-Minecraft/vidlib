package dev.latvian.mods.vidlib.feature.config;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Range;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class RangeConfigValue<T> extends ConfigValue<T, Range> {
	public final Range range;
	public final boolean slider;

	public RangeConfigValue(String name, @Nullable Range range, boolean slider, Function<T, Range> getter, BiConsumer<T, Range> setter) {
		super(name, Range.CODEC, getter, setter);
		this.range = range;
		this.slider = slider;
	}

	@Override
	public <O> Component valueComponent(DynamicOps<O> ops, Range value) {
		if (range == Range.FULL) {
			return Component.literal(KMath.format(value.min() * 100F) + "% - " + KMath.format(value.max() * 100F) + "%").withStyle(ChatFormatting.GOLD);
		}

		return Component.literal(KMath.format(value.min()) + " - " + KMath.format(value.max())).withStyle(ChatFormatting.GOLD);
	}
}
