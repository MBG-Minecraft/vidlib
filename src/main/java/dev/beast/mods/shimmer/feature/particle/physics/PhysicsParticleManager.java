package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerBlockState;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.shader.ShaderHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_SETUP)
public class PhysicsParticleManager implements Consumer<CompiledShaderProgram> {
	public static final VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Normal", VertexFormatElement.NORMAL)
		.build();

	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager(Shimmer.id("physics_particle/solid"), 0, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager(Shimmer.id("physics_particle/cutout"), 1, false);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager(Shimmer.id("physics_particle/translucent"), 2, false);

	public static void renderAll(PhysicsParticleRenderContext ctx) {
		var lightmapTextureManager = ctx.mc().gameRenderer.lightTexture();
		lightmapTextureManager.turnOnLightLayer();
		RenderSystem.enableDepthTest();
		var matrix = RenderSystem.getModelViewStack();
		matrix.pushMatrix();
		matrix.mul(ctx.poseStack().last().pose());

		SOLID.render(matrix, ctx);
		CUTOUT.render(matrix, ctx);
		TRANSLUCENT.render(matrix, ctx);

		matrix.popMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		lightmapTextureManager.turnOffLightLayer();
	}

	public static void tickAll(Level level) {
		SOLID.tick(level);
		CUTOUT.tick(level);
		TRANSLUCENT.tick(level);
	}

	@AutoInit(AutoInit.Type.ASSETS_RELOADED)
	public static void clearAll() {
		ShimmerBlockState.shimmer$clearAllCache();
		SOLID.clear();
		CUTOUT.clear();
		TRANSLUCENT.clear();
	}

	public static PhysicsParticleManager of(BlockState state) {
		if (state.getBlock() instanceof GrassBlock) {
			return SOLID;
		} else if (state.getBlock() == Blocks.WATER) {
			return TRANSLUCENT;
		}

		var rl = ItemBlockRenderTypes.getChunkRenderType(state);

		if (rl == RenderType.translucent() || rl == RenderType.tripwire()) {
			return TRANSLUCENT;
		} else if (rl == RenderType.cutout() || rl == RenderType.cutoutMipped()) {
			return CUTOUT;
		} else {
			return SOLID;
		}
	}

	public final ResourceLocation id;
	public final ShaderHolder shader;
	public final int blend;
	public final boolean mipmaps;
	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;

	private Uniform pProjection, pModel, pTint;
	private float prevTintR, prevTintG, prevTintB, prevTintA;

	public PhysicsParticleManager(ResourceLocation id, int blend, boolean mipmaps) {
		this.id = id;
		this.shader = new ShaderHolder(id, FORMAT);
		this.shader.addListener(this);
		this.blend = blend;
		this.mipmaps = mipmaps;
		this.particles = new ArrayList<>();
		this.queue = new ArrayList<>();
		this.prevTintR = this.prevTintG = this.prevTintB = this.prevTintA = 1F;
	}

	@Override
	public void accept(CompiledShaderProgram program) {
		pProjection = program.PROJECTION_MATRIX;
		pModel = program.MODEL_VIEW_MATRIX;
		pTint = program.COLOR_MODULATOR;
	}

	public void setTint(float r, float g, float b, float a) {
		if (prevTintR != r || prevTintG != g || prevTintB != b || prevTintA != a) {
			prevTintR = r;
			prevTintG = g;
			prevTintB = b;
			prevTintA = a;
			pTint.set(r, g, b, a);
			pTint.upload();
		}
	}

	public void setModelMatrix(Matrix4f m) {
		pModel.set(m);
		pModel.upload();
	}

	public void render(Matrix4fStack matrix, PhysicsParticleRenderContext ctx) {
		if (particles.isEmpty()) {
			return;
		}

		var program = shader.get();

		if (program == null) {
			return;
		}

		RenderSystem.setShader(program);

		if (blend == 2) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			if (Minecraft.useShaderTransparency()) {
				ctx.mc().levelRenderer.getTranslucentTarget().bindWrite(false);
			}

		} else {
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}

		RenderSystem.depthMask(true);

		var tex = ctx.mc().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		tex.setFilter(false, mipmaps);
		RenderSystem.setShaderTexture(0, tex.getId());

		pProjection.set(ctx.projectionMatrix());
		program.bindSampler("Sampler0", RenderSystem.getShaderTexture(0));
		program.apply();

		prevTintR = prevTintG = prevTintB = prevTintA = 1F;
		pTint.set(1F, 1F, 1F, 1F);
		pTint.upload();

		for (var p : particles) {
			p.render(matrix, ctx);
		}

		program.clear();

		if (blend == 2) {
			if (Minecraft.useShaderTransparency()) {
				ctx.mc().getMainRenderTarget().bindWrite(false);
			}
		}

		RenderSystem.defaultBlendFunc();

		tex.setFilter(false, false);
		RenderSystem.setShaderTexture(0, tex.getId());
	}

	public void tick(Level level) {
		particles.addAll(queue);
		queue.clear();

		var it = particles.iterator();

		while (it.hasNext()) {
			var p = it.next();

			if (p.tick(level)) {
				it.remove();
			}
		}
	}

	public String toString() {
		return id.toString();
	}

	public void clear() {
		particles.clear();
		queue.clear();
	}
}
