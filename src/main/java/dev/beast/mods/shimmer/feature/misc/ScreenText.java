package dev.beast.mods.shimmer.feature.misc;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.feature.config.BooleanConfigValue;
import dev.beast.mods.shimmer.feature.config.ConfigValue;
import dev.beast.mods.shimmer.feature.config.FloatConfigValue;
import dev.beast.mods.shimmer.feature.config.IntConfigValue;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.Range;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.ScreenCorner;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ScreenText {
	public static class ScreenTextList {
		public final ScreenText parent;
		public final List<Component> list = new ArrayList<>(0);

		private ScreenTextList(ScreenText parent) {
			this.parent = parent;
		}

		public void add(Component component) {
			list.add(component);
		}

		public void add(String text) {
			list.add(Component.literal(text));
		}

		public void addValue(String name, Tag value) {
			list.add(Component.literal(name + ": ").append(NbtUtils.toPrettyComponent(value)));
		}

		public <T> void addValue(String name, T value, Codec<T> codec) {
			add(name + ": " + codec.encodeStart(parent.ops, value).getOrThrow());
		}

		public void addValue(String name, Object value, ChatFormatting color) {
			list.add(Component.literal(name + ": ").append(Component.literal(String.valueOf(value)).withStyle(color)));
		}

		public void addValue(String name, boolean value) {
			addValue(name, value, value ? ChatFormatting.GREEN : ChatFormatting.RED);
		}

		public void addValue(String name, Number value) {
			addValue(name, value, ChatFormatting.GOLD);
		}

		public void addValue(String name, String value) {
			addValue(name, value, ChatFormatting.YELLOW);
		}

		public <T> void addConfig(T instance, List<ConfigValue<T, ?>> config) {
			for (var value : config) {
				if (value instanceof BooleanConfigValue c) {
					addValue(value.name, (boolean) c.getter.apply(instance));
				} else if (value instanceof FloatConfigValue c && c.slider && c.range == Range.FULL) {
					addValue(value.name, KMath.format(((float) c.getter.apply(instance)) * 100F) + "%", ChatFormatting.GOLD);
				} else if (value instanceof FloatConfigValue c) {
					addValue(value.name, (float) c.getter.apply(instance));
				} else if (value instanceof IntConfigValue c) {
					addValue(value.name, (int) c.getter.apply(instance));
				} else {
					addValue(value.name, Cast.to(value.getter.apply(instance)), value.codec);
				}
			}
		}
	}

	public static final ScreenText RENDER = new ScreenText();
	public static final ScreenText CLIENT_TICK = new ScreenText();

	public DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
	public final ScreenTextList topLeft = new ScreenTextList(this);
	public final ScreenTextList topRight = new ScreenTextList(this);
	public final ScreenTextList bottomLeft = new ScreenTextList(this);
	public final ScreenTextList bottomRight = new ScreenTextList(this);

	public void clear() {
		topLeft.list.clear();
		topRight.list.clear();
		bottomLeft.list.clear();
		bottomRight.list.clear();
	}

	public void addAll(ScreenText from) {
		topLeft.list.addAll(from.topLeft.list);
		topRight.list.addAll(from.topRight.list);
		bottomLeft.list.addAll(from.bottomLeft.list);
		bottomRight.list.addAll(from.bottomRight.list);
	}

	public ScreenTextList get(ScreenCorner corner) {
		return switch (corner) {
			case TOP_LEFT -> topLeft;
			case TOP_RIGHT -> topRight;
			case BOTTOM_LEFT -> bottomLeft;
			case BOTTOM_RIGHT -> bottomRight;
		};
	}
}
