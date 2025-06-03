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
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.StaticBuffers;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.WithCache;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
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
import java.util.function.Supplier;

public class StructureRenderer implements WithCache {
	private static final EnumMap<TerrainRenderLayer, CachedLayer> EMPTY_LAYERS = new EnumMap<>(TerrainRenderLayer.class);

	private record StateModel(BlockPos pos, BlockState state, BlockStateModel model, long seed) {
	}

	private record FluidModel(BlockPos pos, BlockState state, FluidState fluid) {
	}

	private record BuildingLayer(ByteBufferBuilder memory, BufferBuilder bufferBuilder, RenderType type, Map<StateModel, List<BlockModelPart>> parts, int sort) {
		private static final BuildingLayer[] EMPTY = new BuildingLayer[0];
	}

	private record CachedLayer(TerrainRenderLayer layer, RenderType type, StaticBuffers buffer) {
		private static final CachedLayer[] EMPTY = new CachedLayer[0];
	}

	public static class FluidTransformingVertexPipeline extends TransformingVertexPipeline {
		public FluidTransformingVertexPipeline(VertexConsumer parent, Transformation transformation) {
			super(parent, transformation);
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
	private AABB renderBounds = AABB.INFINITE;

	private StructureRenderer(ResourceLocation id, Supplier<StructureHolder> structureProvider) {
		this.id = id;
		this.structureProvider = structureProvider;
		this.origin = BlockPos.ZERO;
	}

	public void preRender(StructureRendererData data) {
		var mc = Minecraft.getInstance();

		if (mc.level == null) {
			return;
		}

		if (layers == null) {
			layers = EMPTY_LAYERS;
			renderBounds = AABB.INFINITE;
			var structure = structureProvider.get();

			if (structure != null) {
				rendering = true;
				buildLevel(mc, structure, data);
			} else {
				rendering = false;
			}

			renderingAll = null;
		}
	}

	private void buildLevel(Minecraft mc, StructureHolder structure, StructureRendererData data) {
		CompletableFuture.runAsync(() -> buildLayers(mc, structure.withoutInvisibleBlocks(), data), Util.backgroundExecutor());
	}

	private void buildLayers(Minecraft mc, StructureHolder structure, StructureRendererData data) {
		var start = System.currentTimeMillis();
		var blockRenderer = mc.getBlockRenderer();
		var random = RandomSource.create();

		var allTypes = RenderType.chunkBufferLayers();
		var layerMap = new Reference2ObjectOpenHashMap<RenderType, BuildingLayer>(allTypes.size());
		var layerSorting = new Reference2IntOpenHashMap<RenderType>(allTypes.size());

		for (int i = 0; i < allTypes.size(); i++) {
			layerSorting.put(allTypes.get(i), i);
		}

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (var entry : structure.blocks().long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();

			var stateModel = new StateModel(pos, state, blockRenderer.getBlockModel(state), state.getSeed(pos));
			random.setSeed(stateModel.seed);
			boolean added = false;

			for (var part : stateModel.model.collectParts(random)) {
				var type = part.getRenderType(state);
				var layer = layerMap.get(type);

				if (layer == null) {
					var memory = new ByteBufferBuilder(65536);
					var bufferBuilder = new BufferBuilder(memory, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
					layer = new BuildingLayer(memory, bufferBuilder, type, new Reference2ObjectArrayMap<>(1), layerSorting.getOrDefault(type, 9999));
					layerMap.put(type, layer);
				}

				layer.parts.computeIfAbsent(stateModel, k -> new ArrayList<>(1)).add(part);
				added = true;
			}

			if (added) {
				minX = Math.min(minX, pos.getX() - 1D);
				minY = Math.min(minY, pos.getY() - 1D);
				minZ = Math.min(minZ, pos.getZ() - 1D);
				maxX = Math.max(maxX, pos.getX() + 2D);
				maxY = Math.max(maxY, pos.getY() + 2D);
				maxZ = Math.max(maxZ, pos.getZ() + 2D);
			}
		}

		renderBounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

		var buildingLayerArray = layerMap.values().toArray(BuildingLayer.EMPTY);
		Arrays.sort(buildingLayerArray, Comparator.comparingInt(BuildingLayer::sort));

		var level = new StructureRendererLevel(structure.blocks(), data.skyLight(), data.blockLight(), mc.level.registryAccess().get(Biomes.PLAINS).get().value());

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
		}

		var time = System.currentTimeMillis() - start;
		mc.execute(() -> upload(buildingLayerArray, time));
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

	public AABB getRenderBounds() {
		return renderBounds;
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
			preRender(data);
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
