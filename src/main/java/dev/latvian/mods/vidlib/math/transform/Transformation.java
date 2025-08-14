package dev.latvian.mods.vidlib.math.transform;

import dev.latvian.mods.klib.math.Vec3f;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public sealed interface Transformation permits EmptyTransformation, TranslateTransformation, RotateTransformation, ScaleTransformation {
	Transformation NONE = EmptyTransformation.INSTANCE;

	static Transformation translate(Vec3f vec) {
		return vec.isZero() ? NONE : new TranslateTransformation(vec);
	}

	static Transformation rotate(float angle, Direction.Axis axis) {
		return angle % 360F == 0F ? NONE : new RotateTransformation(angle, axis);
	}

	static Transformation scale(Vec3f vec) {
		return vec.equals(Vec3f.ONE) ? NONE : new ScaleTransformation(vec);
	}

	void transformMatrix4f(Matrix4f matrix);

	void transformPoseStack(Object poseStack);
}
