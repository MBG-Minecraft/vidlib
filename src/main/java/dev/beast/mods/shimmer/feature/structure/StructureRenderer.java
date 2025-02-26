package dev.beast.mods.shimmer.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class StructureRenderer {
	private record BuildingLayer(RenderType type, ByteBufferBuilder memory, BufferBuilder builder) {
	}

	private record CachedLayer(RenderType type, VertexBuffer buffer) {
		private static final CachedLayer[] EMPTY = new CachedLayer[0];
	}

	private final ClientStructureStorage storage;
	private final ResourceLocation id;
	public boolean mirrorLevel;
	public boolean centerX;
	public boolean centerY;
	public boolean centerZ;
	public boolean cull;
	public boolean removeInnerBlocks;
	public BlockPos origin;

	private Vec3i size = null;
	private CachedLayer[] layers = null;

	StructureRenderer(ClientStructureStorage storage, ResourceLocation id) {
		this.storage = storage;
		this.id = id;
		this.mirrorLevel = false;
		this.centerX = true;
		this.centerY = false;
		this.centerZ = true;
		this.cull = true;
		this.removeInnerBlocks = true;
		this.origin = BlockPos.ZERO;
	}

	public Vec3i getSize() {
		if (size == null) {
			size = Vec3i.ZERO;
			var template = storage.get(id);

			if (template != null) {
				size = template.getSize();
			}
		}

		return size;
	}

	private static boolean isTransparent(@Nullable BlockState state) {
		return state == null || !state.canOcclude() || !state.useShapeForLightOcclusion() || state.getBlock().getClass() != Block.class || state.getRenderShape() != RenderShape.MODEL;
	}

	public void preRender() {
		var mc = Minecraft.getInstance();

		if (mc.level == null) {
			return;
		}

		if (layers == null) {
			layers = CachedLayer.EMPTY;
			var structure = storage.get(id);

			if (structure != null) {
				size = structure.getSize();
				var blockRenderer = mc.getBlockRenderer();
				var blocks = new Long2ObjectOpenHashMap<BlockState>();

				for (var palette : structure.palettes) {
					for (var info : palette.blocks()) {
						if (!info.state().isAir() && info.state().getRenderShape() != RenderShape.INVISIBLE) {
							blocks.put(info.pos().offset(origin).asLong(), info.state());
						}
					}
				}

				if (removeInnerBlocks) {
					var newBlocks = new Long2ObjectOpenHashMap<BlockState>();

					for (var entry : blocks.long2ObjectEntrySet()) {
						var pos = BlockPos.of(entry.getLongKey());
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();

						if (isTransparent(blocks.get(BlockPos.asLong(x - 1, y, z)))
							|| isTransparent(blocks.get(BlockPos.asLong(x + 1, y, z)))
							|| isTransparent(blocks.get(BlockPos.asLong(x, y - 1, z)))
							|| isTransparent(blocks.get(BlockPos.asLong(x, y + 1, z)))
							|| isTransparent(blocks.get(BlockPos.asLong(x, y, z - 1)))
							|| isTransparent(blocks.get(BlockPos.asLong(x, y, z + 1)))
						) {
							newBlocks.put(pos.asLong(), entry.getValue());
						}
					}

					blocks = newBlocks;
				}

				var phantomWorld = new StructureRendererLevel(mc.level, mirrorLevel, blocks, false);
				var random = RandomSource.create();

				var layerMap = new Reference2ObjectOpenHashMap<RenderType, BuildingLayer>(RenderType.chunkBufferLayers().size());
				var localMatrixStack = new PoseStack();

				for (var entry : blocks.long2ObjectEntrySet()) {
					var pos = BlockPos.of(entry.getLongKey());
					var relPos = pos.subtract(origin);
					var state = entry.getValue();

					var model = blockRenderer.getBlockModel(state);

					random.setSeed(state.getSeed(pos));

					for (var type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
						var layer = layerMap.get(type);

						if (layer == null) {
							var memory = new ByteBufferBuilder(type.bufferSize());
							layer = new BuildingLayer(type, memory, new BufferBuilder(memory, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK));
							layerMap.put(type, layer);
						}

						localMatrixStack.setIdentity();
						localMatrixStack.translate(relPos.getX(), relPos.getY(), relPos.getZ());
						blockRenderer.renderBatched(state, pos, phantomWorld, localMatrixStack, layer.builder, cull, random, ModelData.EMPTY, type);
					}
				}

				var list = new ArrayList<CachedLayer>(layerMap.size());

				for (var layer : layerMap.values()) {
					var built = layer.builder.build();

					if (built != null) {
						var vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
						vertexBuffer.bind();
						vertexBuffer.upload(built);
						VertexBuffer.unbind();
						list.add(new CachedLayer(layer.type, vertexBuffer));
					}

					layer.memory.close();
				}

				layers = list.toArray(CachedLayer.EMPTY);
			}
		}
	}

	public void close() {
		if (layers != null) {
			for (var layer : layers) {
				layer.buffer.close();
			}

			layers = null;
		}
	}

	public void render(PoseStack ms) {
		if (layers == null) {
			preRender();
		}

		if (layers == null || layers.length == 0) {
			return;
		}

		var shader = RenderSystem.getShader();

		if (shader == null) {
			return;
		}

		if (centerX || centerY || centerZ) {
			ms.pushPose();
			var x = centerX ? -size.getX() / 2D : 0D;
			var y = centerY ? -size.getY() / 2D : 0D;
			var z = centerZ ? -size.getZ() / 2D : 0D;
			ms.translate(x, y, z);
		}

		var model = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(ms.last().pose());
		var projection = RenderSystem.getProjectionMatrix();

		for (var layer : layers) {
			layer.type.setupRenderState();
			layer.buffer.bind();
			layer.buffer.drawWithShader(model, projection, shader);
			VertexBuffer.unbind();
			layer.type.clearRenderState();
		}

		if (centerX || centerY || centerZ) {
			ms.popPose();
		}
	}
}
