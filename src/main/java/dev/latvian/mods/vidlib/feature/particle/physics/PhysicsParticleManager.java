package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.BlockUtils;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.FrustumIntersection;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager {
	public static final PhysicsParticleManager CUTOUT_MIPPED = new PhysicsParticleManager("Cutout Mipped", TerrainRenderLayer.CUTOUT_MIPPED, PhysicsParticlesRenderTypes.CUTOUT_MIPPED, true);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", TerrainRenderLayer.TRANSLUCENT, PhysicsParticlesRenderTypes.TRANSLUCENT, true);
	public static final PhysicsParticleManager TRIPWIRE = new PhysicsParticleManager("Tripwire", TerrainRenderLayer.TRIPWIRE, PhysicsParticlesRenderTypes.TRANSLUCENT, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", TerrainRenderLayer.CUTOUT, PhysicsParticlesRenderTypes.CUTOUT, false);
	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager("Solid", TerrainRenderLayer.SOLID, PhysicsParticlesRenderTypes.SOLID, true);

	public static final ImBoolean VISIBLE = new ImBoolean(true);
	public static final double SQRT_2 = Math.sqrt(2);

	private static final EnumMap<TerrainRenderLayer, PhysicsParticleManager> ALL = new EnumMap<>(TerrainRenderLayer.class);

	public static void register(PhysicsParticleManager manager) {
		ALL.put(manager.terrainLayer, manager);
	}

	static {
		register(CUTOUT_MIPPED);
		register(TRANSLUCENT);
		register(TRIPWIRE);
		register(CUTOUT);
		register(SOLID);
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

		var rl = getChunkRenderType(state);

		if (rl == ChunkSectionLayer.CUTOUT) {
			return CUTOUT;
		} else if (rl == ChunkSectionLayer.TRANSLUCENT) {
			return TRANSLUCENT;
		} else {
			return SOLID;
		}
	}

	private static ChunkSectionLayer getChunkRenderType(BlockState state) {
		var fluidState = state.getFluidState();

		if (!fluidState.isEmpty()) {
			return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState).layer();
		}

		if (state.getRenderShape() != RenderShape.MODEL) {
			return ChunkSectionLayer.SOLID;
		}

		var parts = new ArrayList<BlockStateModelPart>();
		var random = RandomSource.create(state.getSeed(BlockPos.ZERO));
		Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, random, parts);
		var layer = ChunkSectionLayer.SOLID;

		for (var part : parts) {
			for (var quad : part.getQuads(null)) {
				if (quad.materialInfo().layer() == ChunkSectionLayer.TRANSLUCENT) {
					return ChunkSectionLayer.TRANSLUCENT;
				} else if (quad.materialInfo().layer() == ChunkSectionLayer.CUTOUT) {
					layer = ChunkSectionLayer.CUTOUT;
				}
			}

			for (var direction : Direction.values()) {
				for (var quad : part.getQuads(direction)) {
					if (quad.materialInfo().layer() == ChunkSectionLayer.TRANSLUCENT) {
						return ChunkSectionLayer.TRANSLUCENT;
					} else if (quad.materialInfo().layer() == ChunkSectionLayer.CUTOUT) {
						layer = ChunkSectionLayer.CUTOUT;
					}
				}
			}
		}

		return layer;
	}

	public final TerrainRenderLayer terrainLayer;
	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;
	private final RenderType fallbackRenderType;
	public final String displayName;
	public final boolean mipmaps;
	public int rendered;

	public PhysicsParticleManager(String displayName, TerrainRenderLayer terrainLayer, RenderType fallbackRenderType, boolean mipmaps) {
		this.particles = new ArrayList<>();
		this.queue = new ArrayList<>();

		this.displayName = displayName;
		this.terrainLayer = terrainLayer;
		this.fallbackRenderType = fallbackRenderType;
		this.mipmaps = mipmaps;
	}

	private void render(Minecraft mc, FrameInfo frame, MultiBufferSource bufferSource) {
		var level = mc.level;

		var currentType = fallbackRenderType;
		var consumer = bufferSource.getBuffer(currentType);
		var poseStack = frame.poseStack();
		float delta = frame.worldDelta();
		double camX = frame.cameraX();
		double camY = frame.cameraY();
		double camZ = frame.cameraZ();
		var frustum = frame.frustum();

		var mutablePos = new BlockPos.MutableBlockPos();
		var tempNormal = new Vector3f();

		for (PhysicsParticle p : particles) {
			float dScale = KMath.lerp(delta, p.prevScale, p.scale);

			if (dScale < 0.001F) {
				continue;
			}

			double rx = KMath.lerp(delta, p.prevX, p.x);
			double ry = KMath.lerp(delta, p.prevY, p.y);
			double rz = KMath.lerp(delta, p.prevZ, p.z);

			double dist = Vector2d.distanceSquared(rx, rz, camX, camZ);

			if (dist > p.renderDistanceSq) {
				continue;
			}

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

			int light = LightCoordsUtil.FULL_BRIGHT;

			mutablePos.set(p.x, p.y, p.z);

			if (level != null) {
				light = BlockUtils.getPackedLight(level, mutablePos);
			}

			int lightU = light & 0xFFFF;
			int lightV = (light >> 16) & 0xFFFF;

			p.shape.render(mc, consumer, poseStack.last(), tempNormal, p.red, p.green, p.blue, p.alpha, lightU, lightV);
			poseStack.popPose();
			rendered++;
		}

		mc.renderBuffers().bufferSource().endBatch(currentType);
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
