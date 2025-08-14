package dev.latvian.mods.vidlib.math.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.Vec3f;
import org.joml.Matrix4f;

public record TranslateTransformation(Vec3f pos) implements Transformation {
	@Override
	public void transformMatrix4f(Matrix4f matrix) {
		matrix.translate(pos.x(), pos.y(), pos.z());
	}

	@Override
	public void transformPoseStack(Object poseStack) {
		((PoseStack) poseStack).translate(pos.x(), pos.y(), pos.z());
	}
}
