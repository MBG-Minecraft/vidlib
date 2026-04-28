package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.FrustumIntersection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager {
	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager("Solid", TerrainRenderLayer.SOLID, RenderType.solid(), true);
	public static final PhysicsParticleManager CUTOUT_MIPPED = new PhysicsParticleManager("Cutout Mipped", TerrainRenderLayer.CUTOUT_MIPPED, RenderType.cutoutMipped(), true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", TerrainRenderLayer.CUTOUT, RenderType.cutout(), false);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", TerrainRenderLayer.TRANSLUCENT, RenderType.translucent(), true);
	public static final PhysicsParticleManager TRIPWIRE = new PhysicsParticleManager("Tripwire", TerrainRenderLayer.TRIPWIRE, RenderType.tripwire(), true);

	public static final ImBoolean VISIBLE = new ImBoolean(true);
	public static final double SQRT_2 = Math.sqrt(2);

	private static final EnumMap<TerrainRenderLayer, PhysicsParticleManager> ALL = new EnumMap<>(TerrainRenderLayer.class);

	public static void register(PhysicsParticleManager manager) {
		ALL.put(manager.terrainLayer, manager);
	}

	static {
		register(SOLID);
		register(CUTOUT_MIPPED);
		register(CUTOUT);
		register(TRANSLUCENT);
		register(TRIPWIRE);
	}

	public static void debugInfo(Consumer<String> left, Consumer<String> right) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			return;
		}

		int total = 0;
		int totalRendered = 0;

		for (var manager : ALL.values()) {
			total += manager.particles.size();
			totalRendered += manager.rendered;
			left.accept("%,d/%,d %s".formatted(manager.rendered, manager.particles.size(), manager.displayName));
		}

		right.accept("%,d/%,d Total".formatted(totalRendered, total));
	}

	public static void render(FrameInfo frame) {
		var manager = ALL.get(frame.layer());

		if (manager != null) {
			manager.rendered = 0;

			if (!manager.particles.isEmpty() && VISIBLE.get()) {
				MultiBufferSource.BufferSource bufferSource = frame.mc().renderBuffers().bufferSource();
				bufferSource.endBatch();
				manager.render(frame.mc(), frame, bufferSource);
				bufferSource.endBatch();
			}
		}
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

		if (rl == RenderType.cutoutMipped()) {
			return CUTOUT_MIPPED;
		} else if (rl == RenderType.cutout()) {
			return CUTOUT;
		} else if (rl == RenderType.translucent()) {
			return TRANSLUCENT;
		} else if (rl == RenderType.tripwire()) {
			return TRIPWIRE;
		} else {
			return SOLID;
		}
	}

	public final TerrainRenderLayer terrainLayer;
	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;
	public final RenderType renderType;
	public final String displayName;
	public final boolean mipmaps;
	public int rendered;

	public PhysicsParticleManager(String displayName, TerrainRenderLayer terrainLayer, RenderType renderType, boolean mipmaps) {
		this.particles = new ArrayList<>();
		this.queue = new ArrayList<>();

		this.displayName = displayName;
		this.terrainLayer = terrainLayer;
		this.renderType = renderType;
		this.mipmaps = mipmaps;
	}

	private void render(Minecraft mc, FrameInfo frame, MultiBufferSource bufferSource) {
		var level = mc.level;
		var texture = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		texture.setFilter(false, mipmaps);
		RenderSystem.setShaderTexture(0, texture.getTexture());

		var consumer = bufferSource.getBuffer(renderType);
		var poseStack = frame.poseStack();
		float delta = frame.worldDelta();
		double camX = frame.cameraX();
		double camY = frame.cameraY();
		double camZ = frame.cameraZ();
		var frustum = frame.frustum();

		var mutablePos = new BlockPos.MutableBlockPos();

		for (PhysicsParticle p : particles) {
			float dScale = KMath.lerp(delta, p.prevScale, p.scale);

			if (dScale < 0.001F) {
				continue;
			}

			double rx = KMath.lerp(delta, p.prevX, p.x);
			double ry = KMath.lerp(delta, p.prevY, p.y);
			double rz = KMath.lerp(delta, p.prevZ, p.z);
			double ro = dScale * SQRT_2;
			int cubeInFrustum = frustum.cubeInFrustum(rx - ro, ry - ro, rz - ro, rx + ro, ry + ro, rz + ro);

			if (cubeInFrustum != FrustumIntersection.INSIDE && cubeInFrustum != FrustumIntersection.INTERSECT) {
				continue;
			}

			poseStack.pushPose();
			poseStack.translate((float) (rx - camX), (float) (ry - camY), (float) (rz - camZ));

			if (p.rotationAngle != 0F) {
				poseStack.mulPose(Axis.YP.rotation(p.rotationAngle));
			}

			float dSpin = KMath.lerp(delta, p.prevSpin, p.spin);

			if (dSpin != 0F) {
				poseStack.mulPose(Axis.XP.rotation(dSpin));
			}

			if (p.rotationRoll != 0F) {
				poseStack.mulPose(Axis.ZP.rotation(p.rotationRoll));
			}

			if (dScale != 1F) {
				poseStack.scale(dScale, dScale, dScale);
			}

			int light = LightTexture.FULL_BRIGHT;

			if (level != null) {
				light = level.vl$getPackedLight(mutablePos.set(rx, ry, rz));
			}

			p.shape.render(mc, consumer, poseStack.last().pose(), p.red, p.green, p.blue, p.alpha, light);
			poseStack.popPose();
			rendered++;
		}

		texture.setFilter(false, false);
		RenderSystem.setShaderTexture(0, texture.getTexture());
	}

	public void tick(Level level, long gameTime) {
		if (!queue.isEmpty()) {
			particles.addAll(queue);
			queue.clear();
			particles.sort(PhysicsParticle.COMPARATOR);
		}
		particles.removeIf(p -> p.tick(level, gameTime));
	}

	public void clear() {
		particles.clear();
		queue.clear();
	}

	@Override
	public String toString() {
		return displayName;
	}
}
