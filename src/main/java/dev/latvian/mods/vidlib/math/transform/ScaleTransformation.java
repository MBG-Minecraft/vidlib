package dev.latvian.mods.vidlib.math.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.Vec3f;
import org.joml.Matrix4f;

public record ScaleTransformation(Vec3f scale) implements Transformation {
	@Override
	public void transformMatrix4f(Matrix4f matrix) {
		matrix.scale(scale.x(), scale.y(), scale.z());
	}

	@Override
	public void transformPoseStack(Object poseStack) {
		((PoseStack) poseStack).scale(scale.x(), scale.y(), scale.z());
	}
}
