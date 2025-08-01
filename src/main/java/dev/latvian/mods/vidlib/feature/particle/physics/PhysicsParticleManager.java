package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.klib.gl.IndexBuffer;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import imgui.type.ImBoolean;
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
import java.util.EnumMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager {
	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager("Solid", TerrainRenderLayer.SOLID, PhysicsParticlesRenderTypes.PHYSICS_SOLID, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", TerrainRenderLayer.CUTOUT, PhysicsParticlesRenderTypes.PHYSICS_CUTOUT, false);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", TerrainRenderLayer.TRANSLUCENT, PhysicsParticlesRenderTypes.PHYSICS_TRANSLUCENT, true);
	private static final EnumMap<TerrainRenderLayer, PhysicsParticleManager> ALL = new EnumMap<>(TerrainRenderLayer.class);

	public static void register(PhysicsParticleManager manager) {
		ALL.put(manager.terrainLayer, manager);
	}

	static {
		register(SOLID);
		register(CUTOUT);
		register(TRANSLUCENT);
	}

	public static final ImBoolean VISIBLE = new ImBoolean(true);

	public static void debugInfo(Consumer<String> left, Consumer<String> right) {
		int total = 0;
		int totalRendered = 0;
		int totalBuffersSwitched = 0;

		for (var manager : ALL.values()) {
			total += manager.particles.size();
			totalRendered += manager.rendered;
			totalBuffersSwitched += manager.buffersSwitched;
			left.accept("%,d/%,d [%dx] %s".formatted(manager.rendered, manager.particles.size(), manager.buffersSwitched, manager.displayName));
		}

		right.accept("%,d/%,d [%dx] Total".formatted(totalRendered, total, totalBuffersSwitched));
	}

	public static void render(FrameInfo frame) {
		var manager = ALL.get(frame.layer());

		if (manager == null) {
			return;
		}

		var lightmapTextureManager = frame.mc().gameRenderer.lightTexture();
		lightmapTextureManager.turnOnLightLayer();
		var matrix = RenderSystem.getModelViewStack();
		matrix.pushMatrix();
		// matrix.mul(frame.poseStack().last().pose());
		manager.render(matrix, frame);
		matrix.popMatrix();
		lightmapTextureManager.turnOffLightLayer();
	}

	public static void tickAll(Level level, long gameTime) {
		for (var manager : ALL.values()) {
			manager.tick(level, gameTime);
		}
	}

	@AutoInit(AutoInit.Type.ASSETS_LOADED)
	public static void clearAll() {
		VLBlockState.vl$clearAllCache();
		clearAllParticles();
	}

	public static void clearAllParticles() {
		for (var manager : ALL.values()) {
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
	public final TerrainRenderLayer terrainLayer;
	public final RenderType renderType;
	public final boolean mipmaps;
	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;
	public int rendered;
	public int buffersSwitched;
	public IndexBuffer indexBuffer;

	public PhysicsParticleManager(String displayName, TerrainRenderLayer terrainLayer, RenderType renderType, boolean mipmaps) {
		this.displayName = displayName;
		this.terrainLayer = terrainLayer;
		this.renderType = renderType;
		this.mipmaps = mipmaps;
		this.particles = new ArrayList<>();
		this.queue = new ArrayList<>();
	}

	public void render(Matrix4fStack matrix, FrameInfo frame) {
		rendered = 0;
		buffersSwitched = 0;

		if (particles.isEmpty() || !VISIBLE.get()) {
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
			renderPass.setUniform("ProjMat", new Matrix4f(frame.projectionMatrix()));
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

		particles.removeIf(p -> p.tick(level, gameTime));
	}

	public String toString() {
		return displayName;
	}

	public void clear() {
		particles.clear();
		queue.clear();
	}
}
