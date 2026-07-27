package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.gl.StaticBuffers;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLRenderType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.TerrainRenderTypes;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.VLBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.model.pipeline.TransformingVertexPipeline;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class StructureRenderer implements WithCache {
	private static final EnumMap<TerrainRenderLayer, CachedLayer> EMPTY_LAYERS = new EnumMap<>(TerrainRenderLayer.class);

	private record StateModel(BlockPos pos, BlockState state, BlockStateModel model, long seed) {
	}

	private record BuildingLayer(ByteBufferBuilder memory, BufferBuilder bufferBuilder, ChunkSectionLayer chunkLayer, RenderType type, int sort) {
		private static final BuildingLayer[] EMPTY = new BuildingLayer[0];
	}

	private record CachedLayer(TerrainRenderLayer layer, RenderType type, StaticBuffers buffer) {
	}

	public static class FluidTransformingVertexPipeline extends TransformingVertexPipeline {
		public FluidTransformingVertexPipeline(VertexConsumer parent, Transformation transformation) {
			super(parent, transformation);
		}

		public int wrap() {
			return 0xFFFFFFFF;
		}
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

	public static StructureRenderer create(Ref<LazyStructures> ref) {
		return create(ref.id(), StructureHolder.refSupplier(ref));
	}

	public static StructureRenderer create(Identifier id, Identifier structure) {
		return create(id, StructureHolder.refSupplier(StructureStorage.CLIENT.ref(structure)));
	}

	public static StructureRenderer create(Identifier id) {
		return create(id, id);
	}

	private static TerrainRenderLayer terrainLayer(ChunkSectionLayer layer) {
		return switch (layer) {
			case SOLID -> TerrainRenderLayer.SOLID;
			case CUTOUT -> TerrainRenderLayer.CUTOUT;
			case TRANSLUCENT -> TerrainRenderLayer.TRANSLUCENT;
		};
	}

	private static RenderType renderType(ChunkSectionLayer layer) {
		return TerrainRenderTypes.get(terrainLayer(layer), false).apply(TextureAtlas.LOCATION_BLOCKS);
	}

	private static BuildingLayer getOrCreateLayer(Map<ChunkSectionLayer, BuildingLayer> layerMap, ChunkSectionLayer chunkLayer) {
		var layer = layerMap.get(chunkLayer);

		if (layer == null) {
			var memory = new ByteBufferBuilder(chunkLayer.bufferSize());
			var bufferBuilder = new BufferBuilder(memory, VertexFormat.Mode.QUADS, chunkLayer.vertexFormat());
			layer = new BuildingLayer(memory, bufferBuilder, chunkLayer, renderType(chunkLayer), chunkLayer.ordinal());
			layerMap.put(chunkLayer, layer);
		}

		return layer;
	}

	private static StructureRenderer createGhost(Identifier structure) {
		return new StructureRenderer(structure, StructureHolder.refSupplier(StructureStorage.CLIENT.ref(structure)));
	}

	@AutoInit(AutoInit.Type.CHUNKS_RENDERED)
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

	private EnumMap<TerrainRenderLayer, CachedLayer> layers = null;
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
		var blockRenderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), data.cull(), mc.getBlockColors());
		var blockModels = mc.getModelManager().getBlockStateModelSet();
		var fluidRenderer = new FluidRenderer(mc.getModelManager().getFluidStateModelSet());

		var level = new StructureRendererLevel(structure.blocks(), data.skyLight(), data.blockLight(), VLBiomes.VOID);

		var layerMap = new EnumMap<ChunkSectionLayer, BuildingLayer>(ChunkSectionLayer.class);
		double offsetX = data.centerX() ? -structure.size().getX() / 2D : 0D;
		double offsetY = data.centerY() ? -structure.size().getY() / 2D : 0D;
		double offsetZ = data.centerZ() ? -structure.size().getZ() / 2D : 0D;

		BlockQuadOutput output = (x, y, z, quad, instance) -> {
			var builder = getOrCreateLayer(layerMap, quad.materialInfo().layer()).bufferBuilder;
			builder.putBlockBakedQuad(x, y, z, quad, instance);
		};

		BlockQuadOutput opaqueOutput = (x, y, z, quad, instance) -> {
			var builder = getOrCreateLayer(layerMap, ChunkSectionLayer.SOLID).bufferBuilder;
			builder.putBlockBakedQuad(x, y, z, quad, instance);
		};

		FluidRenderer.Output fluidOutput = chunkLayer -> {
			var layer = getOrCreateLayer(layerMap, chunkLayer);
			var poseStack = new PoseStack();
			poseStack.translate(offsetX, offsetY, offsetZ);
			return new FluidTransformingVertexPipeline(layer.bufferBuilder, new Transformation(poseStack.last().pose()));
		};

		for (var entry : structure.blocks().long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();

			var stateModel = new StateModel(pos, state, blockModels.get(state), state.getSeed(pos));

			blockRenderer.tesselateBlock(
				ModelBlockRenderer.forceOpaque(!mc.options.cutoutLeaves().get(), state) ? opaqueOutput : output,
				(float) (pos.getX() + offsetX),
				(float) (pos.getY() + offsetY),
				(float) (pos.getZ() + offsetZ),
				level,
				pos,
				state,
				stateModel.model,
				stateModel.seed
			);

			var fluidState = state.getFluidState();

			if (!fluidState.isEmpty()) {
				var customRenderer = mc.getModelManager().getFluidStateModelSet().get(fluidState).customRenderer();

				if (customRenderer == null || !customRenderer.renderFluid(fluidRenderer, fluidState, level, pos, fluidOutput, state)) {
					fluidRenderer.tesselate(level, pos, fluidOutput, state, fluidState);
				}
			}
		}

		var buildingLayerArray = layerMap.values().toArray(BuildingLayer.EMPTY);
		Arrays.sort(buildingLayerArray, Comparator.comparingInt(BuildingLayer::sort));

		var time = System.currentTimeMillis() - start;

		if (renderExecutor == backgroundExecutor) {
			upload(buildingLayerArray, time);
		} else {
			renderExecutor.execute(() -> upload(buildingLayerArray, time));
		}
	}

	private void upload(BuildingLayer[] buildingLayerArray, long buildTime) {
		long start = System.currentTimeMillis();

		var layers0 = new EnumMap<>(EMPTY_LAYERS);

		for (var layer : buildingLayerArray) {
			try (var meshData = layer.bufferBuilder.build()) {
				if (meshData != null) {
					try (var memory = new ByteBufferBuilder(6)) {
						if (layer.type.sortOnUpload()) {
							meshData.sortQuads(memory, RenderSystem.getProjectionType().vertexSorting());
						}

						var cachedBuffers = StaticBuffers.of(meshData, () -> "StructureRenderer");
						var terrainLayer = terrainLayer(layer.chunkLayer);
						layers0.put(terrainLayer, new CachedLayer(terrainLayer, layer.type, cachedBuffers));
					}
				}
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
			preRender(mc, data, mc, Util.nonCriticalIoPool());
		}

		var layers0 = layers;

		if (layers0 == null || layers0.isEmpty()) {
			return;
		}

		var layer = layers0.get(renderLayerFilter);

		if (layer == null) {
			return;
		}

		var modelViewMatrix = RenderSystem.getModelViewStack();
		modelViewMatrix.pushMatrix();
		modelViewMatrix.mul(ms.last().pose());

		var renderTarget = layer.type.outputTarget().getRenderTarget();

		try (var renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(
				() -> "Structure renderer " + id,
				renderTarget.getColorTextureView(),
				OptionalInt.empty(),
				renderTarget.useDepth ? renderTarget.getDepthTextureView() : null,
				OptionalDouble.empty()
			)
		) {
			renderPass.setPipeline(layer.type.pipeline());
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1F, 1F, 1F, 1F), new Vector3f(), new Matrix4f()));
			var texture = Minecraft.getInstance().getTextureManager().getTexture(((VLRenderType) layer.type).vl$getTextureSafe());
			renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
			renderPass.bindTexture("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			layer.buffer.setIndexBuffer(renderPass, layer.type.pipeline());
			renderPass.setVertexBuffer(0, layer.buffer.vertexBuffer());
			renderPass.drawIndexed(0, 0, layer.buffer.indexCount(), 1);
		}

		modelViewMatrix.popMatrix();
	}
}
