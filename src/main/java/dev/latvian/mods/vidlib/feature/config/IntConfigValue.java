package dev.latvian.mods.vidlib.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class IntConfigValue<T> extends ConfigValue<T, Integer> {
	public final IntRange range;
	public final boolean slider;

	public IntConfigValue(String name, @Nullable IntRange range, boolean slider, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
		super(name, Codec.INT, getter, setter);
		this.range = range;
		this.slider = slider;
	}

	@Override
	public <O> Component valueComponent(DynamicOps<O> ops, Integer value) {
		return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GOLD);
	}
}
