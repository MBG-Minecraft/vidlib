package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.klib.math.SplitBox;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

public class PhysicsParticleShape {
	private static final ResourceLocation GRASS = VidLib.id("block/grass");

	private final float[] vertexData = new float[24 * 8]; // pos.xyz, normal.xyz, u, v

	public PhysicsParticleShape(BlockState state, SplitBox box) {
		UV baseUv = computeBaseUV(state);

		int idx = 0;
		for (int face = 0; face < 6; face++) {
			var faceShape = box.shape().face(face);
			Vector3fc[] pts = { faceShape.a(), faceShape.b(), faceShape.c(), faceShape.d() };
			Vector3fc n = faceShape.n();

			UV faceUv = multiplyUV(baseUv, box.uvs()[face]);
			float u0 = faceUv.u0(), v0 = faceUv.v0(), u1 = faceUv.u1(), v1 = faceUv.v1();
			float[] us = { u0, u0, u1, u1 };
			float[] vs = { v0, v1, v1, v0 };

			for (int i = 0; i < 4; i++) {
				Vector3fc p = pts[i];
				vertexData[idx++] = p.x();
				vertexData[idx++] = p.y();
				vertexData[idx++] = p.z();
				vertexData[idx++] = n.x();
				vertexData[idx++] = n.y();
				vertexData[idx++] = n.z();
				vertexData[idx++] = us[i];
				vertexData[idx++] = vs[i];
			}
		}
	}

	private UV computeBaseUV(BlockState state) {
		Minecraft mc = Minecraft.getInstance();
		TextureAtlasSprite sprite;
		if (state.getBlock() instanceof GrassBlock) {
			sprite = mc.getBlockAtlas().getSprite(GRASS);
		} else {
			sprite = mc.getBlockRenderer().getBlockModel(state).particleIcon();
		}
		return new UV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
	}

	private UV multiplyUV(UV a, UV b) {
		float u0 = a.u0() + (a.u1() - a.u0()) * b.u0();
		float u1 = a.u0() + (a.u1() - a.u0()) * b.u1();
		float v0 = a.v0() + (a.v1() - a.v0()) * b.v0();
		float v1 = a.v0() + (a.v1() - a.v0()) * b.v1();
		return new UV(u0, v0, u1, v1);
	}

	public void render(VertexConsumer consumer, Matrix4f pose, float r, float g, float b, float a, int light) {
		int lightV = (light >> 16) & 0xFFFF;
		int lightU = light & 0xFFFF;

		int idx = 0;
		for (int i = 0; i < 24; i++) {
			float x = vertexData[idx++];
			float y = vertexData[idx++];
			float z = vertexData[idx++];
			float nx = vertexData[idx++];
			float ny = vertexData[idx++];
			float nz = vertexData[idx++];
			float u = vertexData[idx++];
			float v = vertexData[idx++];

			consumer.addVertex(pose, x, y, z)
				.setUv2(lightU, lightV)
				.setNormal(nx, ny, nz)
				.setColor(r, g, b, a)
				.setUv(u, v);
		}
	}
}
