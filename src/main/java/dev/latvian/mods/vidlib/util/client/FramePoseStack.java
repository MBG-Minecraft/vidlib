package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public interface FramePoseStack {
	record Fixed(PoseStack poseStack, double cameraX, double cameraY, double cameraZ) implements FramePoseStack {
	}

	static FramePoseStack of(PoseStack pose, Vec3 cam) {
		return new Fixed(pose, cam.x, cam.y, cam.z);
	}

	static FramePoseStack of(PoseStack pose) {
		return of(pose, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
	}

	PoseStack poseStack();

	double cameraX();

	double cameraY();

	double cameraZ();

	default float x(double x) {
		return (float) (x - cameraX());
	}

	default float y(double y) {
		return (float) (y - cameraY());
	}

	default float z(double z) {
		return (float) (z - cameraZ());
	}

	default void translate(double x, double y, double z) {
		poseStack().translate(x - cameraX(), y - cameraY(), z - cameraZ());
	}

	default void translate(Vec3 pos) {
		translate(pos.x, pos.y, pos.z);
	}

	default double distanceSq(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		double dx = Math.max(Math.max(minX - cameraX(), cameraX() - maxX), 0D);
		double dy = Math.max(Math.max(minY - cameraY(), cameraY() - maxY), 0D);
		double dz = Math.max(Math.max(minZ - cameraZ(), cameraZ() - maxZ), 0D);
		return dx * dx + dy * dy + dz * dz;
	}
}
