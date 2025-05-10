package dev.latvian.mods.vidlib.feature.particle.physics;


import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.kmath.SplitBox;
import dev.latvian.mods.kmath.texture.UV;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.StaticBuffers;
import dev.latvian.mods.vidlib.util.WithCache;
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
			var s = state.getBlock() instanceof GrassBlock ? mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(VidLib.id("block/grass")) : mc.getBlockRenderer().getBlockModel(state).particleIcon();
			var suv = new UV(s.getU0(), s.getV0(), s.getU1(), s.getV1());

			try (var memory = new ByteBufferBuilder(format.getVertexSize() * 4 * 6)) {
				var buffer = new BufferBuilder(memory, VertexFormat.Mode.QUADS, format);

				for (int i = 0; i < 6; i++) {
					var f = box.shape().face(i);
					var uv = suv.mul(box.uvs()[i]);

					buffer.addVertex(f.a().x(), f.a().y(), f.a().z()).setUv(uv.u0(), uv.v0()).setNormal(f.n().x(), f.n().y(), f.n().z());
					buffer.addVertex(f.b().x(), f.b().y(), f.b().z()).setUv(uv.u0(), uv.v1()).setNormal(f.n().x(), f.n().y(), f.n().z());
					buffer.addVertex(f.c().x(), f.c().y(), f.c().z()).setUv(uv.u1(), uv.v1()).setNormal(f.n().x(), f.n().y(), f.n().z());
					buffer.addVertex(f.d().x(), f.d().y(), f.d().z()).setUv(uv.u1(), uv.v0()).setNormal(f.n().x(), f.n().y(), f.n().z());
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
		return state.vl$toString() + ":" + box;
	}
}
