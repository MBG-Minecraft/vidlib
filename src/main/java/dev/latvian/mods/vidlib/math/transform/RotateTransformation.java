package dev.latvian.mods.vidlib.math.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public record RotateTransformation(float degrees, Direction.Axis axis) implements Transformation {
	@Override
	public void transformMatrix4f(Matrix4f matrix) {
		float angle = (float) Math.toRadians(degrees);

		switch (axis) {
			case X -> matrix.rotateX(angle);
			case Y -> matrix.rotateY(angle);
			case Z -> matrix.rotateZ(angle);
		}
	}

	@Override
	public void transformPoseStack(Object poseStack) {
		((PoseStack) poseStack).mulPose((switch (axis) {
			case X -> Axis.XP;
			case Y -> Axis.YP;
			case Z -> Axis.ZP;
		}).rotationDegrees(degrees));
	}
}
