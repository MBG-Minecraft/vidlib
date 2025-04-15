package dev.beast.mods.shimmer.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.util.WithCache;
import dev.beast.mods.shimmer.util.client.StaticBuffers;
import dev.latvian.mods.kmath.color.Color;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

public class StructureRenderer implements WithCache {
	private record StateModel(BlockPos pos, BlockState state, BlockStateModel model, long seed) {
	}

	private record BuildingLayer(RenderType type, Map<StateModel, List<BlockModelPart>> parts, int sort) {
		private static final BuildingLayer[] EMPTY = new BuildingLayer[0];
	}

	private record CachedLayer(RenderType type, StaticBuffers buffer) {
		private static final CachedLayer[] EMPTY = new CachedLayer[0];
	}

	private static final Map<ResourceLocation, StructureRenderer> RUNTIME_RENDERERS = new HashMap<>();

	public static StructureRenderer create(ResourceLocation id, Supplier<StructureHolder> structure) {
		var oldRenderer = RUNTIME_RENDERERS.get(id);

		if (oldRenderer != null) {
			oldRenderer.clearCache();
		}

		var renderer = new StructureRenderer(id, structure);
		RUNTIME_RENDERERS.put(id, renderer);
		return renderer;
	}

	public static StructureRenderer create(ResourceLocation id, ResourceLocation structure) {
		return create(id, StructureHolder.refSupplier(StructureStorage.CLIENT.ref(structure)));
	}

	public static StructureRenderer create(ResourceLocation id) {
		return create(id, id);
	}

	private static StructureRenderer createGhost(
		ResourceLocation structure,
		boolean centerX,
		boolean centerY,
		boolean centerZ,
		boolean cull,
		boolean removeInnerBlocks,
		Color glowing,
		int skyLight,
		int blockLight
	) {
		var renderer = new StructureRenderer(structure, StructureHolder.refSupplier(StructureStorage.CLIENT.ref(structure)));
		renderer.centerX = centerX;
		renderer.centerY = centerY;
		renderer.centerZ = centerZ;
		renderer.cull = cull;
		renderer.removeInnerBlocks = removeInnerBlocks;
		renderer.glowing = glowing;
		renderer.skyLight = skyLight;
		renderer.blockLight = blockLight;
		return renderer;
	}

	@AutoInit(AutoInit.Type.CHUNKS_RELOADED)
	public static void redrawAll() {
		for (var renderer : RUNTIME_RENDERERS.values()) {
			renderer.clearCache();
		}

		for (var gs : GhostStructure.LIST) {
			gs.structure().clearCache();
		}
	}

	private static final Codec<StructureRenderer> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(r -> r.id),
		Codec.BOOL.optionalFieldOf("center_x", true).forGetter(r -> r.centerX),
		Codec.BOOL.optionalFieldOf("center_y", false).forGetter(r -> r.centerY),
		Codec.BOOL.optionalFieldOf("center_z", true).forGetter(r -> r.centerZ),
		Codec.BOOL.optionalFieldOf("cull", true).forGetter(r -> r.cull),
		Codec.BOOL.optionalFieldOf("remove_inner_blocks", false).forGetter(r -> r.removeInnerBlocks),
		Color.CODEC.optionalFieldOf("glowing", Color.TRANSPARENT).forGetter(r -> r.glowing),
		Codec.INT.optionalFieldOf("sky_level", 15).forGetter(r -> r.skyLight),
		Codec.INT.optionalFieldOf("block_level", 15).forGetter(r -> r.blockLight)
	).apply(instance, StructureRenderer::createGhost));

	public static final Codec<StructureRenderer> GHOST_CODEC = Codec.either(ResourceLocation.CODEC, RECORD_CODEC).xmap(either -> either.map(StructureRenderer::create, Function.identity()), Either::right);

	public final ResourceLocation id;
	private final Supplier<StructureHolder> structureProvider;
	public boolean centerX;
	public boolean centerY;
	public boolean centerZ;
	public boolean cull;
	public boolean removeInnerBlocks;
	public Color glowing;
	public int skyLight;
	public int blockLight;

	private CachedLayer[] layers = null;

	private StructureRenderer(ResourceLocation id, Supplier<StructureHolder> structureProvider) {
		this.id = id;
		this.structureProvider = structureProvider;
		this.centerX = true;
		this.centerY = false;
		this.centerZ = true;
		this.cull = true;
		this.removeInnerBlocks = false;
		this.glowing = Color.TRANSPARENT;
		this.skyLight = 15;
		this.blockLight = 15;
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

			var structure = structureProvider.get();

			if (structure != null) {
				buildLevel(mc, structure.withoutInvisibleBlocks());
			}
		}
	}

	private void buildLevel(Minecraft mc, StructureHolder structure) {
		if (removeInnerBlocks) {
			var blocks = structure.blocks();
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

			structure = new StructureHolder(newBlocks, structure.size());
		}

		try (var tempMemory = new ByteBufferBuilder(65536)) {
			buildLayers(mc, structure, tempMemory);
		}
	}

	private void buildLayers(Minecraft mc, StructureHolder structure, ByteBufferBuilder tempMemory) {
		var blockRenderer = mc.getBlockRenderer();

		var level = new StructureRendererLevel(structure.blocks(), skyLight, blockLight, mc.level.registryAccess().get(Biomes.PLAINS).get().value());

		var random = RandomSource.create();

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
					layer = new BuildingLayer(type, new Reference2ObjectArrayMap<>(1), layerSorting.getOrDefault(type, 9999));
					layerMap.put(type, layer);
				}

				layer.parts.computeIfAbsent(stateModel, k -> new ArrayList<>(1)).add(part);
			}
		}

		var buildingLayerArray = layerMap.values().toArray(BuildingLayer.EMPTY);
		Arrays.sort(buildingLayerArray, Comparator.comparingInt(BuildingLayer::sort));
		var list = new ArrayList<CachedLayer>(buildingLayerArray.length);

		var poseStack = new PoseStack();

		if (centerX || centerY || centerZ) {
			var x = centerX ? -structure.size().getX() / 2D : 0D;
			var y = centerY ? -structure.size().getY() / 2D : 0D;
			var z = centerZ ? -structure.size().getZ() / 2D : 0D;
			poseStack.translate(x, y, z);
		}

		for (var layer : buildingLayerArray) {
			var bufferBuilder = new BufferBuilder(tempMemory, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			for (var entry : layer.parts.entrySet()) {
				var model = entry.getKey();
				var parts = entry.getValue();

				poseStack.pushPose();
				poseStack.translate(model.pos.getX(), model.pos.getY(), model.pos.getZ());

				try {
					blockRenderer.renderBatched(model.state, model.pos, level, poseStack, bufferBuilder, cull, parts);
				} catch (Throwable ex) {
					Shimmer.LOGGER.info("Error rendering " + model.state.getBlock().getName().getString() + " structure block at " + model.pos, ex);
				}

				poseStack.popPose();
			}

			try (var meshData = bufferBuilder.build()) {
				if (meshData != null) {
					if (layer.type.sortOnUpload()) {
						meshData.sortQuads(tempMemory, RenderSystem.getProjectionType().vertexSorting());
					}

					var cachedBuffers = StaticBuffers.of(meshData, () -> "StructureRenderer");
					list.add(new CachedLayer(layer.type, cachedBuffers));
				}

				// tempMemory.discard();
			}
		}

		layers = list.toArray(CachedLayer.EMPTY);
	}

	@Override
	public void clearCache() {
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

		var modelViewMatrix = RenderSystem.getModelViewStack();
		modelViewMatrix.pushMatrix();
		modelViewMatrix.mul(ms.last().pose());

		for (var layer : layers) {
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
				renderPass.setIndexBuffer(layer.buffer.indexBuffer().buffer(), layer.buffer.indexBuffer().type());
				renderPass.setVertexBuffer(0, layer.buffer.vertexBuffer());
				renderPass.drawIndexed(0, layer.buffer.indexCount());
			}

			// layer.buffer.bind();
			// layer.buffer.drawWithShader(model, projection, RenderSystem.getShader());
			// VertexBuffer.unbind();
			layer.type.clearRenderState();
		}

		modelViewMatrix.popMatrix();
	}
}
