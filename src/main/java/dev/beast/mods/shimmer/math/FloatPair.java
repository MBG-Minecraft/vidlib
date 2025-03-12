package dev.beast.mods.shimmer.math;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

public record FloatPair(float a, float b) {
	public static final FloatPair ZERO = new FloatPair(0F, 0F);
	public static final FloatPair ONE = new FloatPair(1F, 1F);

	public static FloatPair of(float a, float b) {
		return a == 0F && b == 0F ? ZERO : a == 1F && b == 1F ? ONE : Math.abs(a - b) < 0.0001F ? new FloatPair(a, a) : new FloatPair(a, b);
	}

	public static FloatPair of(float value) {
		return of(value, value);
	}

	public static FloatPair of(Tag nbt) {
		return switch (nbt) {
			case ListTag list -> of(list.getFloat(0), list.getFloat(1));
			case NumericTag num -> of(num.getAsFloat());
			case IntArrayTag arr -> of(arr.get(0).getAsFloat(), arr.get(1).getAsFloat());
			case ByteArrayTag arr -> of(arr.get(0).getAsFloat(), arr.get(1).getAsFloat());
			case null, default -> null;
		};
	}

	public static Tag toNBT(float a, float b) {
		if (a == b) {
			return KMath.efficient(a);
		}

		var mn = KMath.efficient(a);
		var mx = KMath.efficient(b);

		if (mn.getId() == mx.getId()) {
			if (mn.getId() == Tag.TAG_BYTE) {
				return new ByteArrayTag(new byte[]{(byte) a, (byte) b});
			} else if (mn.getId() == Tag.TAG_INT || mn.getId() == Tag.TAG_SHORT) {
				return new IntArrayTag(new int[]{(int) a, (int) b});
			} else {
				var list = new ListTag();
				list.add(mn);
				list.add(mx);
				return list;
			}
		} else {
			var list = new ListTag();
			list.add(FloatTag.valueOf(a));
			list.add(FloatTag.valueOf(b));
			return list;
		}
	}

	public Tag toNBT() {
		return toNBT(a, b);
	}

	@Override
	public String toString() {
		return a == b ? KMath.format(a) : (KMath.format(a) + " & " + KMath.format(b));
	}
}
