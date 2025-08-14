package dev.latvian.mods.vidlib.math.transform;

import org.joml.Matrix4f;

enum EmptyTransformation implements Transformation {
	INSTANCE;

	@Override
	public void transformMatrix4f(Matrix4f matrix) {
	}

	@Override
	public void transformPoseStack(Object poseStack) {
	}
}
