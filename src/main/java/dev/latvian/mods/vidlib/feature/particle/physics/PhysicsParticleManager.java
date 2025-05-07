package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.IndexBuffer;
import dev.latvian.mods.vidlib.util.FrameInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager {
	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager("Solid", PhysicsParticlesRenderTypes.PHYSICS_SOLID, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", PhysicsParticlesRenderTypes.PHYSICS_CUTOUT, false);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", PhysicsParticlesRenderTypes.PHYSICS_TRANSLUCENT, false);
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
		// matrix.mul(frame.poseStack().last().pose());

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
		VLBlockState.vl$clearAllCache();

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
	public final RenderType renderType;
	public final boolean mipmaps;
	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;
	public int rendered;
	public int buffersSwitched;
	public IndexBuffer indexBuffer;

	public PhysicsParticleManager(String displayName, RenderType renderType, boolean mipmaps) {
		this.displayName = displayName;
		this.renderType = renderType;
		this.mipmaps = mipmaps;
		this.particles = new ArrayList<>();
		this.queue = new ArrayList<>();
	}

	public void render(Matrix4fStack matrix, FrameInfo frame) {
		rendered = 0;
		buffersSwitched = 0;

		if (particles.isEmpty()) {
			return;
		}

		renderType.setupRenderState();

		var tex = frame.mc().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		tex.setFilter(false, mipmaps);
		RenderSystem.setShaderTexture(0, tex.getTexture());

		if (indexBuffer == null || indexBuffer.buffer().isClosed()) {
			indexBuffer = IndexBuffer.of(VertexFormat.Mode.QUADS, 4 * 6);
		}

		var renderTarget = renderType.getRenderTarget();

		try (var renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(
				renderTarget.getColorTexture(),
				OptionalInt.empty(),
				renderTarget.useDepth ? renderTarget.getDepthTexture() : null,
				OptionalDouble.empty()
			)
		) {
			renderPass.setPipeline(renderType.getRenderPipeline());
			renderPass.bindSampler("Sampler0", RenderSystem.getShaderTexture(0));
			// renderPass.setUniform("ProjMat", frame.projectionMatrix());
			renderPass.setIndexBuffer(indexBuffer.buffer(), indexBuffer.type());

			var pass = new PhysicsParticleRenderPass(renderPass);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			for (var p : particles) {
				p.render(matrix, frame, pass);
			}

			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}

		renderType.clearRenderState();

		tex.setFilter(false, false);
		RenderSystem.setShaderTexture(0, tex.getTexture());
	}

	public void tick(Level level, long gameTime) {
		if (!queue.isEmpty()) {
			particles.addAll(queue);

			for (var p : queue) {
				p.shape.getBuffers();
			}

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
