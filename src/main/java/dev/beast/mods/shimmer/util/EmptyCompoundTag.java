package dev.beast.mods.shimmer.util;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public final class EmptyCompoundTag extends CompoundTag {
	EmptyCompoundTag() {
		super(Map.of());
	}
}
