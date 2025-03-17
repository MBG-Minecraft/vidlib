package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import dev.beast.mods.shimmer.feature.config.ConfigValue;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DebugText {
	public static class DebugTextList {
		public final DebugText parent;
		public final List<Component> list = new ArrayList<>(0);

		private DebugTextList(DebugText parent) {
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
			addValue(name, codec.encodeStart(parent.ops, value).getOrThrow());
		}

		public void addValue(String name, Object value, ChatFormatting color) {
			list.add(Component.literal(name + ": ").append(Component.literal(String.valueOf(value)).withStyle(color)));
		}

		public void addValue(String name, boolean value) {
			addValue(name, value, value ? ChatFormatting.GREEN : ChatFormatting.RED);
		}

		public void addValue(String name, float value) {
			addValue(name, value, ChatFormatting.GOLD);
		}

		public void addValue(String name, double value) {
			addValue(name, value, ChatFormatting.GOLD);
		}

		public void addValue(String name, String value) {
			addValue(name, value, ChatFormatting.YELLOW);
		}

		public <T> void addConfig(T instance, List<ConfigValue<T, ?>> config) {
			for (var value : config) {
				addValue(value.name, Cast.to(value.getter.apply(instance)), value.codec);
			}
		}
	}

	public static final DebugText RENDER = new DebugText();
	public static final DebugText CLIENT_TICK = new DebugText();

	public DynamicOps<Tag> ops = NbtOps.INSTANCE;
	public final DebugTextList topLeft = new DebugTextList(this);
	public final DebugTextList topRight = new DebugTextList(this);
	public final DebugTextList bottomLeft = new DebugTextList(this);
	public final DebugTextList bottomRight = new DebugTextList(this);

	public void clear() {
		topLeft.list.clear();
		topRight.list.clear();
		bottomLeft.list.clear();
		bottomRight.list.clear();
	}

	public void addAll(DebugText from) {
		topLeft.list.addAll(from.topLeft.list);
		topRight.list.addAll(from.topRight.list);
		bottomLeft.list.addAll(from.bottomLeft.list);
		bottomRight.list.addAll(from.bottomRight.list);
	}
}
