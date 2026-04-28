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
	private record PhysicsParticleVertex(
		float x,
		float y,
		float z,
		float nx,
		float ny,
		float nz,
		float u,
		float v
	) {
	}

	private static final ResourceLocation GRASS = VidLib.id("block/grass");

	private static UV computeBaseUV(Minecraft mc, BlockState state) {
		TextureAtlasSprite sprite;

		if (state.getBlock() instanceof GrassBlock) {
			sprite = mc.getBlockAtlas().getSprite(GRASS);
		} else {
			sprite = mc.getBlockRenderer().getBlockModel(state).particleIcon();
		}

		return new UV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
	}

	private static UV multiplyUV(UV a, UV b) {
		float u0 = a.u0() + (a.u1() - a.u0()) * b.u0();
		float u1 = a.u0() + (a.u1() - a.u0()) * b.u1();
		float v0 = a.v0() + (a.v1() - a.v0()) * b.v0();
		float v1 = a.v0() + (a.v1() - a.v0()) * b.v1();
		return new UV(u0, v0, u1, v1);
	}

	private final BlockState state;
	private final SplitBox box;
	private PhysicsParticleVertex[] vertices;

	public PhysicsParticleShape(BlockState state, SplitBox box) {
		this.state = state;
		this.box = box;
	}

	public void render(Minecraft mc, VertexConsumer consumer, Matrix4f pose, int r, int g, int b, int a, int light) {
		int lightU = light & 0xFFFF;
		int lightV = (light >> 16) & 0xFFFF;
		var data = vertices;

		if (data == null) {
			data = new PhysicsParticleVertex[24];

			var baseUv = computeBaseUV(mc, state);

			for (int face = 0; face < 6; face++) {
				var faceShape = box.shape().face(face);
				var pts = new Vector3fc[]{faceShape.a(), faceShape.b(), faceShape.c(), faceShape.d()};
				var n = faceShape.n();

				var faceUv = multiplyUV(baseUv, box.uvs()[face]);
				float u0 = faceUv.u0();
				float v0 = faceUv.v0();
				float u1 = faceUv.u1();
				float v1 = faceUv.v1();
				var us = new float[]{u0, u0, u1, u1};
				var vs = new float[]{v0, v1, v1, v0};

				for (int i = 0; i < 4; i++) {
					data[face * 4 + i] = new PhysicsParticleVertex(
						pts[i].x(),
						pts[i].y(),
						pts[i].z(),
						n.x(),
						n.y(),
						n.z(),
						us[i],
						vs[i]
					);
				}
			}

			vertices = data;
		}

		for (int i = 0; i < 24; i++) {
			var v = data[i];

			consumer.addVertex(pose, v.x, v.y, v.z)
				.setColor(r, g, b, a)
				.setUv(v.u, v.v)
				.setUv2(lightU, lightV)
				.setNormal(v.nx, v.ny, v.nz)
			;
		}
	}

	public void clearCache() {
		vertices = null;
	}
}
