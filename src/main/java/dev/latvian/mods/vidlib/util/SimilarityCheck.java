package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.kmath.Rotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

@FunctionalInterface
public interface SimilarityCheck<T> {
	SimilarityCheck<?> DEFAULT = Objects::equals;
	SimilarityCheck<Float> FLOAT = SimilarityCheck::areFloatsSimilar;
	SimilarityCheck<Double> DOUBLE = SimilarityCheck::areDoublesSimilar;
	SimilarityCheck<Vec3> VEC_3 = (a, b) -> areDoublesSimilar(a.x, b.x) && areDoublesSimilar(a.y, b.y) && areDoublesSimilar(a.z, b.z);
	SimilarityCheck<Rotation> ROTATION = (a, b) -> areFloatsSimilar(a.yawDeg(), b.yawDeg()) && areFloatsSimilar(a.pitchDeg(), b.pitchDeg()) && areFloatsSimilar(a.rollDeg(), b.rollDeg());
	SimilarityCheck<ItemStack> ITEM_STACK = ItemStack::isSameItemSameComponents;

	static <T> SimilarityCheck<T> getDefault() {
		return (SimilarityCheck<T>) DEFAULT;
	}

	static boolean areFloatsSimilar(float a, float b) {
		return a == b || Math.abs(a - b) <= 0.001F;
	}

	static boolean areDoublesSimilar(double a, double b) {
		return a == b || Math.abs(a - b) <= 0.001D;
	}

	boolean areSimilar(T a, T b);
}
