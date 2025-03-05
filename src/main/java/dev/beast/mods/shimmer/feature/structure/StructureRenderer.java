package dev.beast.mods.shimmer.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StructureRenderer {
	private record BuildingLayer(RenderType type, BufferBuilder builder, int sort) {
		private static final BuildingLayer[] EMPTY = new BuildingLayer[0];
	}

	private record CachedLayer(RenderType type, VertexBuffer buffer) {
		private static final CachedLayer[] EMPTY = new CachedLayer[0];
	}

	private static final Map<ResourceLocation, StructureRenderer> RUNTIME_RENDERERS = new HashMap<>();

	public static StructureRenderer create(ResourceLocation id, ResourceLocation structure) {
		var renderer = RUNTIME_RENDERERS.get(id);

		if (renderer == null) {
			renderer = new StructureRenderer(ClientStructureStorage.CLIENT.structures.reference(structure));
			RUNTIME_RENDERERS.put(id, renderer);
		}

		return renderer;
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
		int lightLevel
	) {
		var renderer = new StructureRenderer(ClientStructureStorage.CLIENT.structures.reference(structure));
		renderer.centerX = centerX;
		renderer.centerY = centerY;
		renderer.centerZ = centerZ;
		renderer.cull = cull;
		renderer.removeInnerBlocks = removeInnerBlocks;
		renderer.glowing = glowing;
		renderer.lightLevel = lightLevel;
		return renderer;
	}

	public static void redrawAll() {
		for (var renderer : RUNTIME_RENDERERS.values()) {
			renderer.close();
		}

		for (var gs : GhostStructure.LIST) {
			gs.structure().close();
		}
	}

	private static final Codec<StructureRenderer> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(r -> r.structureRef.id()),
		Codec.BOOL.optionalFieldOf("center_x", true).forGetter(r -> r.centerX),
		Codec.BOOL.optionalFieldOf("center_y", false).forGetter(r -> r.centerY),
		Codec.BOOL.optionalFieldOf("center_z", true).forGetter(r -> r.centerZ),
		Codec.BOOL.optionalFieldOf("cull", true).forGetter(r -> r.cull),
		Codec.BOOL.optionalFieldOf("remove_inner_blocks", false).forGetter(r -> r.removeInnerBlocks),
		Color.CODEC.optionalFieldOf("glowing", Color.TRANSPARENT).forGetter(r -> r.glowing),
		Codec.INT.optionalFieldOf("light_level", 15).forGetter(r -> r.lightLevel)
	).apply(instance, StructureRenderer::createGhost));

	public static final Codec<StructureRenderer> GHOST_CODEC = Codec.either(ResourceLocation.CODEC, RECORD_CODEC).xmap(either -> either.map(StructureRenderer::create, Function.identity()), Either::right);

	private final RegistryReference<ResourceLocation, Lazy<StructureTemplate>> structureRef;
	public boolean centerX;
	public boolean centerY;
	public boolean centerZ;
	public boolean cull;
	public boolean removeInnerBlocks;
	public Color glowing;
	public int lightLevel;

	private CachedLayer[] layers = null;

	private StructureRenderer(@Nullable RegistryReference<ResourceLocation, Lazy<StructureTemplate>> structureRef) {
		this.structureRef = structureRef;
		this.centerX = true;
		this.centerY = false;
		this.centerZ = true;
		this.cull = true;
		this.removeInnerBlocks = false;
		this.glowing = Color.TRANSPARENT;
		this.lightLevel = 15;
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

			var structure = structureRef == null ? null : structureRef.get().get();

			if (structure != null) {
				buildWorld(mc, structure);
			}
		}
	}

	private void buildWorld(Minecraft mc, StructureTemplate structure) {
		var blocks = new Long2ObjectOpenHashMap<BlockState>();

		for (var palette : structure.palettes) {
			for (var info : palette.blocks()) {
				if (!info.state().isAir() && info.state().getRenderShape() != RenderShape.INVISIBLE) {
					blocks.put(info.pos().asLong(), info.state());
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

		try (var tempMemory = new ByteBufferBuilder(65536)) {
			buildLayers(mc, blocks, tempMemory);
		}
	}

	private void buildLayers(Minecraft mc, Long2ObjectMap<BlockState> blocks, ByteBufferBuilder tempMemory) {
		var blockRenderer = mc.getBlockRenderer();

		var level = new StructureRendererLevel(blocks, lightLevel, mc.level.registryAccess().registry(Registries.BIOME).get().get(Biomes.PLAINS));
		var random = RandomSource.create();

		var allTypes = RenderType.chunkBufferLayers();

		var layerMap = new Reference2ObjectOpenHashMap<RenderType, BuildingLayer>(allTypes.size());
		var layerSorting = new Reference2IntOpenHashMap<RenderType>(allTypes.size());

		for (int i = 0; i < allTypes.size(); i++) {
			layerSorting.put(allTypes.get(i), i);
		}

		var poseStack = new PoseStack();

		for (var entry : blocks.long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();

			var model = blockRenderer.getBlockModel(state);

			random.setSeed(state.getSeed(pos));

			for (var type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
				var layer = layerMap.get(type);

				if (layer == null) {
					layer = new BuildingLayer(type, new BufferBuilder(tempMemory, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK), layerSorting.getOrDefault(type, 9999));
					layerMap.put(type, layer);
				}

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

				try {
					blockRenderer.renderBatched(state, pos, level, poseStack, layer.builder, cull, random, ModelData.EMPTY, type);
				} catch (Throwable ex) {
					Shimmer.LOGGER.info("Error rendering structure block at " + pos, ex);
				}

				poseStack.popPose();
			}
		}

		var buildingLayerArray = layerMap.values().toArray(BuildingLayer.EMPTY);
		Arrays.sort(buildingLayerArray, Comparator.comparingInt(BuildingLayer::sort));

		var list = new ArrayList<CachedLayer>(buildingLayerArray.length);

		for (var layer : buildingLayerArray) {
			var meshData = layer.builder.build();

			if (meshData != null) {
				var vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

				if (layer.type.sortOnUpload()) {
					meshData.sortQuads(tempMemory, RenderSystem.getVertexSorting());
				}

				vertexBuffer.bind();
				vertexBuffer.upload(meshData);
				VertexBuffer.unbind();
				list.add(new CachedLayer(layer.type, vertexBuffer));
			}
		}

		layers = list.toArray(CachedLayer.EMPTY);
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

		if (centerX || centerY || centerZ) {
			var structure = structureRef == null ? null : structureRef.get().get();
			var size = structure == null ? Vec3i.ZERO : structure.getSize();
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
			layer.buffer.drawWithShader(model, projection, RenderSystem.getShader());
			VertexBuffer.unbind();
			layer.type.clearRenderState();
		}

		if (centerX || centerY || centerZ) {
			ms.popPose();
		}
	}
}
