package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.beast.mods.shimmer.core.ShimmerBlockState;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.shader.ShaderHolder;
import dev.beast.mods.shimmer.util.FrameInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager implements Consumer<CompiledShaderProgram> {
	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager("Solid", PhysicsParticlesRenderTypes.PHYSICS_SOLID_SHADER, PhysicsParticlesRenderTypes.PHYSICS_SOLID, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", PhysicsParticlesRenderTypes.PHYSICS_CUTOUT_SHADER, PhysicsParticlesRenderTypes.PHYSICS_CUTOUT, false);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", PhysicsParticlesRenderTypes.PHYSICS_TRANSLUCENT_SHADER, PhysicsParticlesRenderTypes.PHYSICS_TRANSLUCENT, false);
	public static final List<PhysicsParticleManager> ALL = new ArrayList<>(4);

	static {
		ALL.add(SOLID);
		ALL.add(CUTOUT);
		ALL.add(TRANSLUCENT);
	}

	public static void debugInfo(Consumer<String> left, Consumer<String> right) {
		int total = 0;
		int totalRendered = 0;
		int totalBuffersSwitched = 0;

		for (var manager : PhysicsParticleManager.ALL) {
			total += manager.particles.size();
			totalRendered += manager.rendered;
			totalBuffersSwitched += manager.buffersSwitched;
			left.accept("%,d/%,d [%dx] %s".formatted(manager.rendered, manager.particles.size(), manager.buffersSwitched, manager.displayName));
		}

		right.accept(Minecraft.getInstance().fpsString.split(" ", 2)[0] + " FPS");
		right.accept("%,d/%,d [%dx] Total".formatted(totalRendered, total, totalBuffersSwitched));
	}

	public static void renderAll(FrameInfo frame) {
		var lightmapTextureManager = frame.mc().gameRenderer.lightTexture();
		lightmapTextureManager.turnOnLightLayer();
		var matrix = RenderSystem.getModelViewStack();
		matrix.pushMatrix();
		matrix.mul(frame.poseStack().last().pose());

		for (var manager : ALL) {
			manager.render(matrix, frame);
		}

		matrix.popMatrix();
		lightmapTextureManager.turnOffLightLayer();
	}

	public static void tickAll(Level level, long gameTime) {
		for (var manager : ALL) {
			manager.tick(level, gameTime);
		}
	}

	@AutoInit(AutoInit.Type.ASSETS_RELOADED)
	public static void clearAll() {
		ShimmerBlockState.shimmer$clearAllCache();

		for (var manager : ALL) {
			manager.clear();
		}
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

	public final String displayName;
	public final ShaderHolder shader;
	public final RenderType renderType;
	public final boolean mipmaps;
	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;
	public int rendered;
	public int buffersSwitched;

	private Uniform pProjection, pModel, pTint;
	private float prevTintR, prevTintG, prevTintB, prevTintA;

	public PhysicsParticleManager(String displayName, ShaderHolder shader, RenderType renderType, boolean mipmaps) {
		this.displayName = displayName;
		this.shader = shader;
		this.shader.addListener(this);
		this.renderType = renderType;
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

	public void render(Matrix4fStack matrix, FrameInfo frame) {
		rendered = 0;
		buffersSwitched = 0;

		if (particles.isEmpty()) {
			return;
		}

		var program = shader.get();

		if (program == null) {
			return;
		}

		RenderSystem.setShader(program);

		renderType.setupRenderState();

		var tex = frame.mc().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		tex.setFilter(false, mipmaps);
		RenderSystem.setShaderTexture(0, tex.getId());

		pProjection.set(frame.projectionMatrix());
		program.bindSampler("Sampler0", RenderSystem.getShaderTexture(0));
		program.apply();

		prevTintR = prevTintG = prevTintB = prevTintA = 1F;
		pTint.set(1F, 1F, 1F, 1F);
		pTint.upload();
		var buffer = new VertexBuffer[1];

		for (var p : particles) {
			p.render(matrix, frame, buffer);
		}

		VertexBuffer.unbind();
		program.clear();
		renderType.clearRenderState();

		tex.setFilter(false, false);
		RenderSystem.setShaderTexture(0, tex.getId());
	}

	public void tick(Level level, long gameTime) {
		if (!queue.isEmpty()) {
			particles.addAll(queue);
			queue.clear();
			particles.sort(PhysicsParticle.COMPARATOR);
		}

		var it = particles.iterator();

		while (it.hasNext()) {
			var p = it.next();

			if (p.tick(level, gameTime)) {
				it.remove();
			}
		}
	}

	public String toString() {
		return displayName;
	}

	public void clear() {
		particles.clear();
		queue.clear();
	}
}
