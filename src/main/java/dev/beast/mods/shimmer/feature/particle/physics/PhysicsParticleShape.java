package dev.beast.mods.shimmer.feature.particle.physics;


import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Split;
import dev.beast.mods.shimmer.math.SplitBox;
import dev.beast.mods.shimmer.math.UV;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PhysicsParticleShape {
	public final BlockState state;
	public final SplitBox box;
	private VertexBuffer vertexBuffer;
	private VertexBuffer fastVertexBuffer;

	public PhysicsParticleShape(BlockState state, SplitBox box) {
		this.state = state;
		this.box = box;
	}

	@Override
	public String toString() {
		return BlockStateParser.serialize(state) + " " + box.split().id + " " + box.index();
	}

	public VertexBuffer getBuffer() {
		if (vertexBuffer == null) {
			var mc = Minecraft.getInstance();
			var s = state.getBlock() instanceof GrassBlock ? mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Shimmer.id("block/grass")) : mc.getBlockRenderer().getBlockModel(state).getParticleIcon();
			var suv = new UV(s.getU0(), s.getV0(), s.getU1(), s.getV1());

			var memory = new ByteBufferBuilder(PhysicsParticlesRenderTypes.FORMAT.getVertexSize() * 4 * 6);
			var buffer = new BufferBuilder(memory, VertexFormat.Mode.QUADS, PhysicsParticlesRenderTypes.FORMAT);

			for (int i = 0; i < 6; i++) {
				var f = box.facePos()[i];
				var uv = suv.mul(box.uvs()[i]);
				var n = Split.NORMALS[i];

				buffer.addVertex(f[0].x(), f[0].y(), f[0].z()).setUv(uv.u0(), uv.v0()).setNormal(n.x(), n.y(), n.z());
				buffer.addVertex(f[1].x(), f[1].y(), f[1].z()).setUv(uv.u0(), uv.v1()).setNormal(n.x(), n.y(), n.z());
				buffer.addVertex(f[2].x(), f[2].y(), f[2].z()).setUv(uv.u1(), uv.v1()).setNormal(n.x(), n.y(), n.z());
				buffer.addVertex(f[3].x(), f[3].y(), f[3].z()).setUv(uv.u1(), uv.v0()).setNormal(n.x(), n.y(), n.z());
			}

			var vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
			vertexBuffer.bind();
			vertexBuffer.upload(buffer.buildOrThrow());
			VertexBuffer.unbind();
			memory.close();

			this.vertexBuffer = vertexBuffer;
		}

		return vertexBuffer;
	}

	public VertexBuffer getFastBuffer() {
		if (fastVertexBuffer == null) {
			var mc = Minecraft.getInstance();
			var s = state.getBlock() instanceof GrassBlock ? mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Shimmer.id("block/grass")) : mc.getBlockRenderer().getBlockModel(state).getParticleIcon();
			var suv = new UV(s.getU0(), s.getV0(), s.getU1(), s.getV1());

			var memory = new ByteBufferBuilder(PhysicsParticlesRenderTypes.FORMAT.getVertexSize() * 4);
			var buffer = new BufferBuilder(memory, VertexFormat.Mode.QUADS, PhysicsParticlesRenderTypes.FORMAT);

			int i = Direction.SOUTH.ordinal();

			var f = box.facePos()[i];
			var uv = suv.mul(box.uvs()[i]);
			var n = Split.NORMALS[i];

			buffer.addVertex(f[0].x(), f[0].y(), 0F).setUv(uv.u0(), uv.v0()).setNormal(n.x(), n.y(), n.z());
			buffer.addVertex(f[1].x(), f[1].y(), 0F).setUv(uv.u0(), uv.v1()).setNormal(n.x(), n.y(), n.z());
			buffer.addVertex(f[2].x(), f[2].y(), 0F).setUv(uv.u1(), uv.v1()).setNormal(n.x(), n.y(), n.z());
			buffer.addVertex(f[3].x(), f[3].y(), 0F).setUv(uv.u1(), uv.v0()).setNormal(n.x(), n.y(), n.z());

			var vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
			vertexBuffer.bind();
			vertexBuffer.upload(buffer.buildOrThrow());
			VertexBuffer.unbind();
			memory.close();

			this.fastVertexBuffer = vertexBuffer;
		}

		return fastVertexBuffer;
	}

	public void clearCache() {
		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}

		if (fastVertexBuffer != null) {
			fastVertexBuffer.close();
			fastVertexBuffer = null;
		}
	}
}
