package dev.beast.mods.shimmer.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BooleanConfigValue<T> extends ConfigValue<T, Boolean> {
	public static final Component TRUE = Component.literal("True").withStyle(ChatFormatting.GREEN);
	public static final Component FALSE = Component.literal("False").withStyle(ChatFormatting.RED);

	public BooleanConfigValue(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		super(name, Codec.BOOL, getter, setter);
	}

	@Override
	public <O> Component valueComponent(DynamicOps<O> ops, Boolean value) {
		return value ? TRUE : FALSE;
	}
}
