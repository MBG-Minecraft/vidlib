package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.vertex.PoseStack;
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
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class PhysicsParticleShape {
	private record PhysicsParticleFace(
		PhysicsParticleVertex a,
		PhysicsParticleVertex b,
		PhysicsParticleVertex c,
		PhysicsParticleVertex d,
		float nx,
		float ny,
		float nz
	) {
	}

	private record PhysicsParticleVertex(
		float x,
		float y,
		float z,
		float u,
		float v
	) {
		public PhysicsParticleVertex(Vector3fc pos, float u, float v) {
			this(pos.x(), pos.y(), pos.z(), u, v);
		}

		public void addVertex(VertexConsumer consumer, Matrix4f m, float nx, float ny, float nz, int r, int g, int b, int a, int lightU, int lightV) {
			consumer.addVertex(m, x, y, z)
				.setColor(r, g, b, a)
				.setUv(u, v)
				.setUv2(lightU, lightV)
				.setNormal(nx, ny, nz);
			;
		}
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
	private PhysicsParticleFace[] faces;

	public PhysicsParticleShape(BlockState state, SplitBox box) {
		this.state = state;
		this.box = box;
	}

	public void render(Minecraft mc, VertexConsumer consumer, PoseStack.Pose pose, Vector3f tempNormal, int r, int g, int b, int a, int lightU, int lightV) {
		var data = faces;

		if (data == null) {
			data = new PhysicsParticleFace[6];

			var baseUv = computeBaseUV(mc, state);

			for (int face = 0; face < 6; face++) {
				var faceShape = box.shape().face(face);

				var faceUv = multiplyUV(baseUv, box.uvs()[face]);
				float u0 = faceUv.u0();
				float v0 = faceUv.v0();
				float u1 = faceUv.u1();
				float v1 = faceUv.v1();

				data[face] = new PhysicsParticleFace(
					new PhysicsParticleVertex(faceShape.a(), u0, v0),
					new PhysicsParticleVertex(faceShape.b(), u0, v1),
					new PhysicsParticleVertex(faceShape.c(), u1, v1),
					new PhysicsParticleVertex(faceShape.d(), u1, v0),
					faceShape.n().x(),
					faceShape.n().y(),
					faceShape.n().z()
				);
			}

			faces = data;
		}

		var m = pose.pose();

		for (int f = 0; f < 6; f++) {
			var face = data[f];
			pose.transformNormal(face.nx, face.ny, face.nz, tempNormal);
			float nx = tempNormal.x;
			float ny = tempNormal.y;
			float nz = tempNormal.z;
			face.a.addVertex(consumer, m, nx, ny, nz, r, g, b, a, lightU, lightV);
			face.b.addVertex(consumer, m, nx, ny, nz, r, g, b, a, lightU, lightV);
			face.c.addVertex(consumer, m, nx, ny, nz, r, g, b, a, lightU, lightV);
			face.d.addVertex(consumer, m, nx, ny, nz, r, g, b, a, lightU, lightV);
		}
	}

	public void clearCache() {
		faces = null;
	}
}
