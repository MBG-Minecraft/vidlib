package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.gl.StaticBuffers;
import dev.latvian.mods.klib.util.Async;
import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.VLBiomes;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.fml.loading.FMLLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class StructureRenderer implements WithCache {
	private static final EnumMap<ChunkSectionLayer, CachedLayer> EMPTY_LAYERS = new EnumMap<>(ChunkSectionLayer.class);

	private record StateModel(BlockPos pos, BlockState state, BlockStateModel model, long seed) {
	}

	private record FluidModel(BlockPos pos, BlockState state, FluidState fluid) {
	}

	private static final class BuildingLayer {
		private final ByteBufferBuilder memory;
		private BufferBuilder bufferBuilder;
		private final ChunkSectionLayer sectionLayer;
		private final Map<StateModel, List<BlockStateModelPart>> parts;
		private final List<FluidModel> fluids;
		private final int sort;

		private static final BuildingLayer[] EMPTY = new BuildingLayer[0];

		private BuildingLayer(ByteBufferBuilder memory, BufferBuilder bufferBuilder, ChunkSectionLayer sectionLayer, Map<StateModel, List<BlockStateModelPart>> parts, List<FluidModel> fluids, int sort) {
			this.memory = memory;
			this.bufferBuilder = bufferBuilder;
			this.sectionLayer = sectionLayer;
			this.parts = parts;
			this.fluids = fluids;
			this.sort = sort;
		}
	}

	private record CachedLayer(ChunkSectionLayer sectionLayer, StaticBuffers buffer) {
	}

	/**
	 * Legacy hook: {@link dev.latvian.mods.vidlib.core.mixin.LiquidBlockRendererMixin} no longer has a
	 * target to hook (26.3 replaced {@code LiquidBlockRenderer} with {@code FluidRenderer}), so the
	 * vertex pipeline is inert here.
	 */
	@Deprecated
	public static class FluidTransformingVertexPipeline {
	}

	private static final Map<Identifier, StructureRenderer> RUNTIME_RENDERERS = new HashMap<>();
	private static Integer renderingAll = null;

	public static StructureRenderer create(Identifier id, Supplier<StructureHolder> structure) {
		var oldRenderer = RUNTIME_RENDERERS.get(id);

		if (oldRenderer != null) {
			oldRenderer.clearCache();
		}

		var renderer = new StructureRenderer(id, structure);
		RUNTIME_RENDERERS.put(id, renderer);
		renderingAll = null;
		return renderer;
	}

	public static StructureRenderer create(Identifier id) {
		return create(id, id);
	}

	@AutoInit(AutoInit.Type.CHUNKS_RELOADED)
	public static void redrawAll() {
		for (var renderer : RUNTIME_RENDERERS.values()) {
			renderer.clearCache();
		}

		for (var gs : GhostStructure.LIST) {
			for (var s : gs.structures()) {
				s.structure().clearCache();
			}
		}

		renderingAll = null;
	}

	public static int getRenderingAll() {
		if (renderingAll == null) {
			int r = 0;

			for (var gs : GhostStructure.LIST) {
				for (var s : gs.structures()) {
					if (s.structure().rendering) {
						r++;
					}
				}
			}

			renderingAll = r;
		}

		return renderingAll;
	}

	public static final Codec<StructureRenderer> GHOST_CODEC = Identifier.CODEC.xmap(StructureRenderer::createGhost, r -> r.id);

	public final Identifier id;
	private final Supplier<StructureHolder> structureProvider;
	public BlockPos origin;

	private EnumMap<ChunkSectionLayer, CachedLayer> layers = null;
	private boolean rendering = false;

	private StructureRenderer(Identifier id, Supplier<StructureHolder> structureProvider) {
		this.id = id;
		this.structureProvider = structureProvider;
		this.origin = BlockPos.ZERO;
	}

	public void preRender(Minecraft mc, StructureRendererData data, Executor renderExecutor, Executor backgroundExecutor) {
		if (layers == null) {
			layers = EMPTY_LAYERS;
			var structure = structureProvider.get();

			if (structure != null) {
				rendering = true;
				buildLevel(mc, structure, data, renderExecutor, backgroundExecutor);
			} else {
				rendering = false;
			}

			renderingAll = null;
		}
	}

	private void buildLevel(Minecraft mc, StructureHolder structure, StructureRendererData data, Executor renderExecutor, Executor backgroundExecutor) {
		if (renderExecutor == backgroundExecutor) {
			buildLayers(mc, structure, data, renderExecutor, backgroundExecutor);
		} else {
			CompletableFuture.runAsync(() -> buildLayers(mc, structure, data, renderExecutor, backgroundExecutor), backgroundExecutor);
		}
	}

	private void buildLayers(Minecraft mc, StructureHolder structure, StructureRendererData data, Executor renderExecutor, Executor backgroundExecutor) {
		var start = System.currentTimeMillis();
		var blockModelSet = mc.getModelManager().getBlockStateModelSet();
		var fluidModelSet = mc.getModelManager().getFluidStateModelSet();
		var random = RandomSource.create();

		var level = new StructureRendererLevel(structure.blocks(), data.skyLight(), data.blockLight(), VLBiomes.VOID);
		var layerMap = new Reference2ObjectOpenHashMap<ChunkSectionLayer, BuildingLayer>();

		var blockRenderer = new ModelBlockRenderer(true, data.cull(), mc.getBlockColors());
		var fluidRenderer = new FluidRenderer(fluidModelSet);

		BlockQuadOutput quadOutput = (x, y, z, quad, instance) -> {
			var builder = getOrBeginLayer(layerMap, quad.materialInfo().layer());
			builder.putBlockBakedQuad(x, y, z, quad, instance);
		};

		FluidRenderer.Output fluidOutput = l -> getOrBeginLayer(layerMap, l);

		for (var entry : structure.blocks().long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();
			var seed = state.getSeed(pos);
			random.setSeed(seed);

			var parts = new ArrayList<BlockStateModelPart>();
			blockModelSet.get(state).collectParts(random, parts);

			if (!parts.isEmpty()) {
				try {
					blockRenderer.tesselateBlock(
						quadOutput,
						pos.getX(),
						pos.getY(),
						pos.getZ(),
						level,
						pos,
						state,
						blockModelSet.get(state),
						seed
					);
				} catch (Throwable ex) {
					VidLib.LOGGER.info("Error rendering " + state.getBlock().getName().getString() + " structure block at " + pos, ex);
				}
			}

			var fluidState = state.getFluidState();

			if (!fluidState.isEmpty()) {
				try {
					fluidRenderer.tesselate(level, pos, fluidOutput, state, fluidState);
				} catch (Throwable ex) {
					VidLib.LOGGER.info("Error rendering " + state.getBlock().getName().getString() + " structure fluid at " + pos, ex);
				}
			}
		}

		var buildingLayerArray = layerMap.values().toArray(BuildingLayer.EMPTY);
		Arrays.sort(buildingLayerArray, (a, b) -> Integer.compare(a.sort, b.sort));

		var poseStack = new PoseStack();

		if (data.centerX() || data.centerY() || data.centerZ()) {
			var x = data.centerX() ? -structure.size().getX() / 2D : 0D;
			var y = data.centerY() ? -structure.size().getY() / 2D : 0D;
			var z = data.centerZ() ? -structure.size().getZ() / 2D : 0D;
			poseStack.translate(x, y, z);
		}

		var time = System.currentTimeMillis() - start;

		if (renderExecutor == backgroundExecutor) {
			upload(buildingLayerArray, time);
		} else {
			renderExecutor.execute(() -> upload(buildingLayerArray, time));
		}
	}

	private static BufferBuilder getOrBeginLayer(
		Map<ChunkSectionLayer, BuildingLayer> layerMap,
		ChunkSectionLayer sectionLayer
	) {
		var layer = layerMap.get(sectionLayer);

		if (layer == null) {
			var memory = new ByteBufferBuilder(Math.max(sectionLayer.bufferSize(), 65536));
			layer = new BuildingLayer(memory, null, sectionLayer, new Reference2ObjectArrayMap<>(1), new ArrayList<>(), sectionLayer.ordinal());
			layerMap.put(sectionLayer, layer);
		}

		if (layer.bufferBuilder == null) {
			var builder = new BufferBuilder(layer.memory, PrimitiveTopology.QUADS, sectionLayer.vertexFormat());
			layer.bufferBuilder = builder;
			return builder;
		}

		return layer.bufferBuilder;
	}

	private void upload(BuildingLayer[] buildingLayerArray, long buildTime) {
		long start = System.currentTimeMillis();

		var layers0 = new EnumMap<>(EMPTY_LAYERS);

		for (var layer : buildingLayerArray) {
			var builder = layer.bufferBuilder;

			if (builder == null) {
				layer.memory.close();
				continue;
			}

			try (var meshData = builder.build()) {
				if (meshData != null) {
					try (var memory = new ByteBufferBuilder(6)) {
						if (layer.sectionLayer.translucent()) {
							meshData.sortQuads(memory, RenderSystem.getProjectionType().vertexSorting());
						}

						var cachedBuffers = StaticBuffers.of(meshData, () -> "StructureRenderer");
						layers0.put(layer.sectionLayer, new CachedLayer(layer.sectionLayer, cachedBuffers));
					}
				}
			} catch (Throwable ex) {
				VidLib.LOGGER.error("Error uploading structure layer " + layer.sectionLayer, ex);
			}

			layer.memory.close();
		}

		layers = layers0;

		long time = System.currentTimeMillis() - start;

		if (!FMLLoader.getCurrent().isProduction()) {
			VidLib.LOGGER.info("%s took %,d ms to build and %,d ms to upload".formatted(id, buildTime, time));
		}

		rendering = false;
		renderingAll = null;
	}

	@Override
	public void clearCache() {
		var layers0 = layers;

		if (layers0 != null) {
			for (var layer : layers0.values()) {
				MiscClientUtils.CLIENT_CLOSEABLE.add(layer.buffer);
			}
		}

		layers = null;
	}

	public void render(PoseStack ms, StructureRendererData data) {
		for (var renderLayerFilter : TerrainRenderLayer.ALL) {
			render(ms, renderLayerFilter, data);
		}
	}

	public void render(PoseStack ms, TerrainRenderLayer renderLayerFilter, StructureRendererData data) {
		if (layers == null) {
			var mc = Minecraft.getInstance();
			preRender(mc, data, mc, Async.EXECUTOR);
		}

		var layers0 = layers;

		if (layers0 == null || layers0.isEmpty()) {
			return;
		}

		var layer = layers0.get(renderLayerFilter);

		if (layer == null) {
			return;
		}

		var mc = Minecraft.getInstance();
		var mainTarget = mc.gameRenderer.mainRenderTarget();
		var atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);

		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.mul(ms.last().pose());

		try {
			var modelView = RenderSystem.getModelViewMatrixCopy();
			var atlasView = atlas.getTextureView();
			int atlasWidth = atlasView == null ? 0 : atlasView.getWidth(0);
			int atlasHeight = atlasView == null ? 0 : atlasView.getHeight(0);

			var terrainTransform = RenderSystem.getDynamicUniforms()
				.writeTerrainTransform(modelView, atlasWidth, atlasHeight);

			var autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
			var defaultIndexBuffer = autoIndices.getBuffer(layer.buffer.indexCount());

			var pipeline = layer.sectionLayer.pipeline(false);

			try (var renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(
					() -> "StructureRenderer",
					mainTarget.getColorTextureView(),
					Optional.empty(),
					mainTarget.getDepthTextureView(),
					OptionalDouble.empty()
				)
			) {
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("TerrainUniform", terrainTransform);
				renderPass.setUniform("Sampler0", atlasView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR, true));
				renderPass.setUniform("Sampler2", mc.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

				renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
				layer.buffer.setIndexBuffer(renderPass, pipeline);
				var vertexBuffer = layer.buffer.vertexBuffer().slice();
				renderPass.setVertexBuffer(0, vertexBuffer);
				renderPass.drawIndexed(layer.buffer.indexCount(), 1, 0, 0, 0);
			}
		} finally {
			modelViewStack.popMatrix();
		}
	}
}
