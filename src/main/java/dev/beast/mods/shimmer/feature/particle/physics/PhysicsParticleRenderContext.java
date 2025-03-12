package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;

public record PhysicsParticleRenderContext(
	Minecraft mc,
	PoseStack poseStack,
	Matrix4f projectionMatrix,
	float delta,
	Camera camera,
	Frustum frustum
) {
}
