package dev.beast.mods.shimmer.feature.particle.physics;


import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.WithCache;
import dev.beast.mods.shimmer.util.client.StaticBuffers;
import dev.latvian.mods.kmath.Split;
import dev.latvian.mods.kmath.SplitBox;
import dev.latvian.mods.kmath.texture.UV;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class PhysicsParticleShape implements WithCache, Supplier<String> {
	public final BlockState state;
	public final SplitBox box;
	private StaticBuffers buffers;

	public PhysicsParticleShape(BlockState state, SplitBox box) {
		this.state = state;
		this.box = box;
	}

	@Override
	public String toString() {
		return BlockStateParser.serialize(state) + " " + box.split().id + " " + box.index();
	}

	@Nullable
	public StaticBuffers getBuffers() {
		if (buffers == null || !buffers.isEmpty() && buffers.vertexBuffer().isClosed()) {
			var format = PhysicsParticlesRenderTypes.FORMAT;
			buffers = StaticBuffers.empty(format);

			var mc = Minecraft.getInstance();
			var s = state.getBlock() instanceof GrassBlock ? mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Shimmer.id("block/grass")) : mc.getBlockRenderer().getBlockModel(state).particleIcon();
			var suv = new UV(s.getU0(), s.getV0(), s.getU1(), s.getV1());

			try (var memory = new ByteBufferBuilder(format.getVertexSize() * 4 * 6)) {
				var buffer = new BufferBuilder(memory, VertexFormat.Mode.QUADS, format);

				for (int i = 0; i < 6; i++) {
					var f = box.facePos()[i];
					var uv = suv.mul(box.uvs()[i]);
					var n = Split.NORMALS[i];

					buffer.addVertex(f[0].x(), f[0].y(), f[0].z()).setUv(uv.u0(), uv.v0()).setNormal(n.x(), n.y(), n.z());
					buffer.addVertex(f[1].x(), f[1].y(), f[1].z()).setUv(uv.u0(), uv.v1()).setNormal(n.x(), n.y(), n.z());
					buffer.addVertex(f[2].x(), f[2].y(), f[2].z()).setUv(uv.u1(), uv.v1()).setNormal(n.x(), n.y(), n.z());
					buffer.addVertex(f[3].x(), f[3].y(), f[3].z()).setUv(uv.u1(), uv.v0()).setNormal(n.x(), n.y(), n.z());
				}

				try (var meshData = buffer.build()) {
					if (meshData != null) {
						buffers = StaticBuffers.of(meshData, this, Optional.empty());
					}
				}
			}
		}

		return buffers.isEmpty() ? null : buffers;
	}

	@Override
	public void clearCache() {
		if (buffers != null) {
			buffers.close();
			buffers = null;
		}
	}

	@Override
	public String get() {
		return state.shimmer$toString() + ":" + box;
	}
}
