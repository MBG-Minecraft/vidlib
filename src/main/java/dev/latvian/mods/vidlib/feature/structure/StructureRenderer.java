package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.gl.StaticBuffers;
import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.VLBiomes;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.model.pipeline.TransformingVertexPipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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

	private record FluidModel(BlockPos pos, BlockState state, FluidState fluid) {
	}

	private record BuildingLayer(ByteBufferBuilder memory, BufferBuilder bufferBuilder, RenderType type, Map<StateModel, List<BlockModelPart>> parts, List<FluidModel> fluids, int sort) {
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

	private static final Map<ResourceLocation, StructureRenderer> RUNTIME_RENDERERS = new HashMap<>();
	private static Integer renderingAll = null;

	public static StructureRenderer create(ResourceLocation id, Supplier<StructureHolder> structure) {
		var oldRenderer = RUNTIME_RENDERERS.get(id);

		if (oldRenderer != null) {
			oldRenderer.clearCache();
		}

		var renderer = new StructureRenderer(id, structure);
		RUNTIME_RENDERERS.put(id, renderer);
		renderingAll = null;
		return renderer;
	}

	public static StructureRenderer create(RegistryRef<LazyStructures> ref) {
		return create(ref.id(), StructureHolder.refSupplier(ref));
	}

	public static StructureRenderer create(ResourceLocation id, ResourceLocation structure) {
		return create(id, StructureHolder.refSupplier(StructureStorage.CLIENT.ref(structure)));
	}

	public static StructureRenderer create(ResourceLocation id) {
		return create(id, id);
	}

	private static StructureRenderer createGhost(ResourceLocation structure) {
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

	public static final Codec<StructureRenderer> GHOST_CODEC = ResourceLocation.CODEC.xmap(StructureRenderer::createGhost, r -> r.id);

	public final ResourceLocation id;
	private final Supplier<StructureHolder> structureProvider;
	public BlockPos origin;

	private EnumMap<TerrainRenderLayer, CachedLayer> layers = null;
	private boolean rendering = false;

	private StructureRenderer(ResourceLocation id, Supplier<StructureHolder> structureProvider) {
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
		var blockRenderer = mc.getBlockRenderer();
		var random = RandomSource.create();

		var biome = VLBiomes.VOID.get();
		var level = new StructureRendererLevel(structure.blocks(), data.skyLight(), data.blockLight(), biome);

		var allTypes = RenderType.chunkBufferLayers();
		var layerMap = new Reference2ObjectOpenHashMap<RenderType, BuildingLayer>(allTypes.size());
		var layerSorting = new Reference2IntOpenHashMap<RenderType>(allTypes.size());

		for (int i = 0; i < allTypes.size(); i++) {
			layerSorting.put(allTypes.get(i), i);
		}

		for (var entry : structure.blocks().long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();

			var stateModel = new StateModel(pos, state, blockRenderer.getBlockModel(state), state.getSeed(pos));
			random.setSeed(stateModel.seed);

			for (var part : stateModel.model.collectParts(random)) {
				var type = part.getRenderType(state);
				var layer = layerMap.get(type);

				if (layer == null) {
					var memory = new ByteBufferBuilder(65536);
					var bufferBuilder = new BufferBuilder(memory, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
					layer = new BuildingLayer(memory, bufferBuilder, type, new Reference2ObjectArrayMap<>(1), new ArrayList<>(), layerSorting.getOrDefault(type, 9999));
					layerMap.put(type, layer);
				}

				layer.parts.computeIfAbsent(stateModel, k -> new ArrayList<>(1)).add(part);
			}

			var fluidState = state.getFluidState();

			if (!fluidState.isEmpty()) {
				var type = ItemBlockRenderTypes.getRenderLayer(fluidState);
				var layer = layerMap.get(type);

				if (layer == null) {
					var memory = new ByteBufferBuilder(65536);
					var bufferBuilder = new BufferBuilder(memory, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
					layer = new BuildingLayer(memory, bufferBuilder, type, new Reference2ObjectArrayMap<>(1), new ArrayList<>(), layerSorting.getOrDefault(type, 9999));
					layerMap.put(type, layer);
				}

				layer.fluids.add(new FluidModel(pos, state, fluidState));
			}
		}

		var buildingLayerArray = layerMap.values().toArray(BuildingLayer.EMPTY);
		Arrays.sort(buildingLayerArray, Comparator.comparingInt(BuildingLayer::sort));

		var poseStack = new PoseStack();

		if (data.centerX() || data.centerY() || data.centerZ()) {
			var x = data.centerX() ? -structure.size().getX() / 2D : 0D;
			var y = data.centerY() ? -structure.size().getY() / 2D : 0D;
			var z = data.centerZ() ? -structure.size().getZ() / 2D : 0D;
			poseStack.translate(x, y, z);
		}

		for (var layer : buildingLayerArray) {
			for (var entry : layer.parts.entrySet()) {
				var model = entry.getKey();
				var parts = entry.getValue();

				poseStack.pushPose();
				poseStack.translate(model.pos.getX(), model.pos.getY(), model.pos.getZ());

				try {
					blockRenderer.renderBatched(model.state, model.pos, level, poseStack, layer.bufferBuilder, data.cull(), parts);
				} catch (Throwable ex) {
					VidLib.LOGGER.info("Error rendering " + model.state.getBlock().getName().getString() + " structure block at " + model.pos, ex);
				}

				poseStack.popPose();
			}

			if (!layer.fluids.isEmpty()) {
				for (var model : layer.fluids) {
					poseStack.pushPose();
					// poseStack.translate(model.pos.getX(), model.pos.getY(), model.pos.getZ());
					var buffer = new FluidTransformingVertexPipeline(layer.bufferBuilder, new Transformation(poseStack.last().pose()));
					blockRenderer.renderLiquid(model.pos, level, buffer, model.state, model.fluid);
					poseStack.popPose();
				}
			}
		}

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
						var terrainLayer = TerrainRenderLayer.fromBlockRenderType(layer.type);
						layers0.put(terrainLayer, new CachedLayer(terrainLayer, layer.type, cachedBuffers));
					}
				}
			}

			layer.memory.close();
		}

		layers = layers0;

		long time = System.currentTimeMillis() - start;

		if (!FMLLoader.isProduction()) {
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
			preRender(mc, data, mc, Util.backgroundExecutor());
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
		layer.type.setupRenderState();

		var renderTarget = layer.type.getRenderTarget();

		try (var renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(
				renderTarget.getColorTexture(),
				OptionalInt.empty(),
				renderTarget.useDepth ? renderTarget.getDepthTexture() : null,
				OptionalDouble.empty()
			)
		) {
			renderPass.setPipeline(layer.type.getRenderPipeline());
			renderPass.bindSampler("Sampler0", RenderSystem.getShaderTexture(0));
			renderPass.bindSampler("Sampler2", RenderSystem.getShaderTexture(2));
			layer.buffer.setIndexBuffer(renderPass, layer.type.getRenderPipeline());
			renderPass.setVertexBuffer(0, layer.buffer.vertexBuffer());
			renderPass.drawIndexed(0, layer.buffer.indexCount());
		}

		layer.type.clearRenderState();
		modelViewMatrix.popMatrix();
	}
}
