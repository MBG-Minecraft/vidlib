package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.util.Async;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.VLBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StructureRenderer implements WithCache {
	public static final Codec<StructureRenderer> GHOST_CODEC = ID.CODEC.xmap(StructureRenderer::createGhost, r -> r.id);
	private static final RenderType CUTOUT_STRUCTURE = createStructureRenderType("cutout_structure", RenderPipelines.CUTOUT_BLOCK, false);
	private static final RenderType TRANSLUCENT_STRUCTURE = createStructureRenderType("translucent_structure", RenderPipelines.TRANSLUCENT_BLOCK, true);
	private static final Map<Identifier, StructureRenderer> RUNTIME_RENDERERS = new HashMap<>();
	private static final Built EMPTY = new Built(List.of(), List.of(), List.of(), List.of(), null, null);
	private static Integer renderingAll = null;
	public final Identifier id;
	private final Supplier<StructureHolder> structureProvider;
	private final Map<Biome, Built> buildCache = new HashMap<>();
	public BlockPos origin;
	private Built built;
	private Biome builtBiome;
	private boolean rendering;
	private int generation;

	private StructureRenderer(Identifier id, Supplier<StructureHolder> structureProvider) {
		this.id = id;
		this.structureProvider = structureProvider;
		this.origin = BlockPos.ZERO;
	}

	private static RenderType createStructureRenderType(String name, RenderPipeline pipeline, boolean translucent) {
		var setup = RenderSetup.builder(pipeline)
			.useLightmap()
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS, () -> RenderSystem.getSamplerCache()
				.getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false))
			.affectsCrumbling()
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);

		if (translucent) {
			setup.sortOnUpload().setOitPipelines(RenderPipelines.OIT_TRANSLUCENT_BLOCK);
		}

		return RenderType.create(name, setup.createRenderSetup());
	}

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

	public static StructureRenderer create(Identifier id, Identifier structure) {
		return create(id, StructureHolder.refSupplier(structure));
	}

	private static StructureRenderer createGhost(Identifier structure) {
		return new StructureRenderer(structure, StructureHolder.refSupplier(structure));
	}

	@AutoInit(AutoInit.Type.CHUNKS_RELOADED)
	public static void redrawAll() {
		for (var renderer : RUNTIME_RENDERERS.values()) {
			renderer.clearCache();
		}

		for (var ghostStructure : GhostStructure.LIST) {
			for (var part : ghostStructure.structures()) {
				part.structure().clearCache();
			}
		}

		renderingAll = null;
	}

	public static int getRenderingAll() {
		if (renderingAll == null) {
			int count = 0;

			for (var ghostStructure : GhostStructure.LIST) {
				for (var part : ghostStructure.structures()) {
					if (part.structure().rendering) {
						count++;
					}
				}
			}

			renderingAll = count;
		}

		return renderingAll;
	}

	public void preRender(StructureRendererData data) {
		ensureBuilt(data);
	}

	@Nullable
	public StructureHolder getStructure() {
		return structureProvider.get();
	}

	private void ensureBuilt(StructureRendererData data) {
		var biome = resolveBiome();

		if (built != null && builtBiome == biome) {
			return;
		}

		if (rendering) {
			return;
		}

		var cached = buildCache.get(biome);

		if (cached != null) {
			built = cached;
			builtBiome = biome;
			return;
		}

		var structure = structureProvider.get();

		if (structure == null || structure.empty()) {
			built = EMPTY;
			builtBiome = biome;
			return;
		}

		rendering = true;
		int currentGeneration = generation;
		var buildBiome = biome;

		Async.supply(() -> {
			try {
				return build(structure, data, buildBiome);
			} catch (Throwable ex) {
				VidLib.LOGGER.error("Error building structure renderer " + id, ex);
				return EMPTY;
			}
		}).thenAccept(result -> Minecraft.getInstance().execute(() -> {
			if (generation != currentGeneration) {
				return;
			}

			buildCache.put(buildBiome, result);
			built = result;
			builtBiome = buildBiome;
			rendering = false;
			renderingAll = null;
		}));
	}

	private Biome resolveBiome() {
		var level = Minecraft.getInstance().level;

		if (level != null) {
			var holder = level.getBiome(origin);

			if (holder.isBound()) {
				return holder.value();
			}
		}

		return VLBiomes.VOID;
	}

	private Built build(StructureHolder structure, StructureRendererData data, Biome biome) {
		var mc = Minecraft.getInstance();
		var quads = new ArrayList<CachedQuad>();
		var fluids = new ArrayList<FluidBlock>();
		var translucentQuads = new ArrayList<CachedQuad>();
		var translucentFluids = new ArrayList<FluidBlock>();
		var level = new StructureRendererLevel(structure.blocks(), data.skyLight(), data.blockLight(), biome);
		var modelRenderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), data.cull(), mc.getBlockColors());
		var models = mc.getModelManager().getBlockStateModelSet();
		var fluidModels = mc.getModelManager().getFluidStateModelSet();
		var fluidRenderer = new FluidRenderer(fluidModels);

		float centerX = data.centerX() ? -structure.size().getX() / 2F : 0F;
		float centerY = data.centerY() ? -structure.size().getY() / 2F : 0F;
		float centerZ = data.centerZ() ? -structure.size().getZ() / 2F : 0F;

		for (var entry : structure.blocks().long2ObjectEntrySet()) {
			var pos = BlockPos.of(entry.getLongKey());
			var state = entry.getValue();
			var model = models.get(state);
			long seed = state.getSeed(pos);

			BlockQuadOutput output = (x, y, z, quad, instance) -> {
				var target = quad.materialInfo().layer() == ChunkSectionLayer.TRANSLUCENT ? translucentQuads : quads;
				target.add(CachedQuad.of(x + centerX, y + centerY, z + centerZ, quad, instance));
			};

			try {
				modelRenderer.tesselateBlock(output, pos.getX(), pos.getY(), pos.getZ(), level, pos, state, model, seed);
			} catch (Throwable ex) {
				VidLib.LOGGER.info("Error rendering structure block at " + pos, ex);
			}

			var fluid = state.getFluidState();

			if (!fluid.isEmpty()) {
				var translucent = fluidModels.get(fluid).layer() == ChunkSectionLayer.TRANSLUCENT;
				(translucent ? translucentFluids : fluids).add(new FluidBlock(pos, state, fluid));
			}
		}

		return new Built(quads, fluids, translucentQuads, translucentFluids, level, fluidRenderer);
	}

	public void submit(SubmitContext ctx, StructureRendererData data) {
		ensureBuilt(data);

		if (built == null) {
			return;
		}

		submitPass(ctx, built.quads, built.fluids, CUTOUT_STRUCTURE);
		submitPass(ctx, built.translucentQuads, built.translucentFluids, TRANSLUCENT_STRUCTURE);
	}

	private void submitPass(SubmitContext ctx, List<CachedQuad> quads, List<FluidBlock> fluids, RenderType renderType) {
		if (quads.isEmpty() && fluids.isEmpty()) {
			return;
		}

		ctx.submit(renderType, (pose, buffer) -> {
			var poseStack = new PoseStack();
			poseStack.mulPose(pose.pose());
			var instance = new QuadInstance();

			for (var quad : quads) {
				instance.setColor(0, quad.color0());
				instance.setColor(1, quad.color1());
				instance.setColor(2, quad.color2());
				instance.setColor(3, quad.color3());
				instance.setLightCoords(0, quad.light0());
				instance.setLightCoords(1, quad.light1());
				instance.setLightCoords(2, quad.light2());
				instance.setLightCoords(3, quad.light3());
				instance.setOverlayCoords(quad.overlay());

				poseStack.pushPose();
				poseStack.translate(quad.x(), quad.y(), quad.z());
				buffer.putBakedQuad(poseStack.last(), quad.quad(), instance);
				poseStack.popPose();
			}

			for (var fluid : fluids) {
				built.fluidRenderer.tesselate(built.level, fluid.pos(), l -> buffer, fluid.state(), fluid.fluid());
			}
		});
	}

	@Override
	public void clearCache() {
		generation++;
		buildCache.clear();
		built = null;
		builtBiome = null;
		rendering = false;
	}

	private record CachedQuad(
		float x, float y, float z,
		BakedQuad quad,
		int color0, int color1, int color2, int color3,
		int light0, int light1, int light2, int light3,
		int overlay
	) {
		private static CachedQuad of(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
			return new CachedQuad(
				x, y, z, quad,
				instance.getColor(0), instance.getColor(1), instance.getColor(2), instance.getColor(3),
				instance.getLightCoords(0), instance.getLightCoords(1), instance.getLightCoords(2), instance.getLightCoords(3),
				instance.overlayCoords()
			);
		}
	}

	private record FluidBlock(BlockPos pos, BlockState state, FluidState fluid) {
	}

	private record Built(
		List<CachedQuad> quads,
		List<FluidBlock> fluids,
		List<CachedQuad> translucentQuads,
		List<FluidBlock> translucentFluids,
		StructureRendererLevel level,
		FluidRenderer fluidRenderer
	) {
	}
}