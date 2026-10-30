package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.BlockUtils;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticleRenderTypes;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager {
	public static final PhysicsParticleManager CUTOUT_MIPPED = new PhysicsParticleManager("Cutout Mipped", VidLibParticleRenderTypes.Physics.CUTOUT_MIPPED, true);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", VidLibParticleRenderTypes.Physics.TRANSLUCENT, true);
	public static final PhysicsParticleManager TRIPWIRE = new PhysicsParticleManager("Tripwire", VidLibParticleRenderTypes.Physics.TRIPWIRE, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", VidLibParticleRenderTypes.Physics.CUTOUT, false);
	public static final PhysicsParticleManager SOLID = new PhysicsParticleManager("Solid", RenderTypes.solidMovingBlock(), true);

	public static final ImBoolean VISIBLE = new ImBoolean(true);
	public static final double SQRT_2 = Math.sqrt(2);

	private static final List<PhysicsParticleManager> ALL = new ArrayList<>();

	public static void register(PhysicsParticleManager manager) {
		ALL.add(manager);
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

		for (var manager : ALL) {
			total += manager.particles.size();
			totalRendered += manager.rendered;
			left.accept("%,d/%,d %s".formatted(manager.rendered, manager.particles.size(), manager.displayName));
		}

		right.accept("%,d/%,d Total".formatted(totalRendered, total));
	}

	public static void tickAll(Level level, long gameTime) {
		for (var manager : ALL) {
			manager.tick(level, gameTime);
		}
	}

	@AutoInit(AutoInit.Type.ASSETS_LOADED)
	public static void clearAll() {
		VLBlockState.vl$clearAllCache();
		clearAllParticles();
	}

	public static void clearAllParticles() {
		for (var manager : ALL) {
			manager.clear();
		}
	}

	public static PhysicsParticleManager of(BlockState state) {
		if (state.getBlock() instanceof GrassBlock) {
			return CUTOUT_MIPPED;
		} else if (state.getBlock() == Blocks.WATER) {
			return TRANSLUCENT;
		} else if (state.getBlock() instanceof LeavesBlock) {
			return CUTOUT_MIPPED;
		} else if (state.getBlock() instanceof TripWireBlock) {
			return TRIPWIRE;
		}

		ChunkSectionLayer layer = ChunkSectionLayer.byTransparency(vl$transparency(state));

		return switch (layer) {
			case TRANSLUCENT -> TRANSLUCENT;
			case CUTOUT -> CUTOUT;
			case SOLID -> SOLID;
		};
	}

	public static void extractAll() {
		for (var manager : ALL) {
			manager.rendered = 0;

			if (!manager.particles.isEmpty() && VISIBLE.get()) {
				MultiBufferSource bufferSource = frame.buffers();
				manager.render(frame.mc(), frame, bufferSource);
			}
		}
	}

	/**
	 * 26.3 removed {@code ItemBlockRenderTypes#getChunkRenderType}; transparency is now a property
	 * of the block's particle sprite, mirroring how {@code ChunkSectionLayer.byTransparency} is
	 * reached from {@code BakedQuad.MaterialInfo}.
	 */
	private static com.mojang.blaze3d.platform.Transparency vl$transparency(BlockState state) {
		var mc = Minecraft.getInstance();
		var sprite = mc.getModelManager().getBlockStateModelSet().get(state).particleMaterial().sprite();
		return sprite == null
			? com.mojang.blaze3d.platform.Transparency.NONE
			: sprite.transparency();
	}

	public final List<PhysicsParticle> particles;
	public final List<PhysicsParticle> queue;
	private final RenderType renderType;
	public final String displayName;
	public final boolean mipmaps;
	public int rendered;

	public PhysicsParticleManager(String displayName, RenderType renderType, boolean mipmaps) {
		this.particles = new ArrayList<>();
		this.queue = new ArrayList<>();

		this.displayName = displayName;
		this.renderType = renderType;
		this.mipmaps = mipmaps;
	}

	@Nullable
	public List<PhysicsParticleRenderState> extract(Minecraft mc, PoseStack poseStack, FrustumCheck frustum, Vec3 cameraPos, float delta) {
		List<PhysicsParticleRenderState> states = null;

		var mutablePos = new BlockPos.MutableBlockPos();

		for (var p : particles) {
			float dScale = KMath.lerp(delta, p.prevScale, p.scale);

			if (dScale < 0.001F) {
				continue;
			}

			double x = KMath.lerp(delta, p.prevX, p.x);
			double y = KMath.lerp(delta, p.prevY, p.y);
			double z = KMath.lerp(delta, p.prevZ, p.z);

			double dist = Vector2d.distanceSquared(x, z, cameraPos.x, cameraPos.z);

			if (dist > p.renderDistanceSq) {
				continue;
			}

			double ro = dScale * SQRT_2;

			if (!frustum.isVisible(x - ro, y - ro, z - ro, x + ro, y + ro, z + ro)) {
				continue;
			}

			var state = new PhysicsParticleRenderState();
			state.x = x;
			state.y = y;
			state.z = z;
			state.scale = dScale;

			if (p.rotationAngle != 0F) {
				poseStack.rotateY(p.rotationAngle);
			}

			float dSpin = KMath.lerp(delta, p.prevSpin, p.spin);

			if (dSpin != 0F) {
				poseStack.rotateX(dSpin);
			}

			if (p.rotationRoll != 0F) {
				poseStack.rotateZ(p.rotationRoll);
			}

			if (dScale != 1F) {
				poseStack.scale(dScale, dScale, dScale);
			}

			int light = net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

			mutablePos.set(p.x, p.y, p.z);

			if (mc.level != null) {
				light = BlockUtils.getPackedLight(mc.level, mutablePos);
			}

			state.shape = p.shape;
			state.red = p.red;
			state.green = p.green;
			state.blue = p.blue;
			state.alpha = p.alpha;
			state.lightU = light & 0xFFFF;
			state.lightV = (light >> 16) & 0xFFFF;
			rendered++;

			if (states == null) {
				states = new LinkedList<>();
			}

			states.add(state);
		}

		return states;
	}

	public void submit(Minecraft mc, SubmitNodeCollector collector, PoseStack poseStack, FrustumCheck frustum, List<PhysicsParticleRenderState> states) {
		// 1.21.5 swapped in the vanilla chunk render type whenever an Iris shader pack was running.
		// 26.3's terrain render types are ChunkSectionLayer pipelines with a different vertex format,
		// so both paths use VidLib's own physics-particle render type.
		var consumer = bufferSource.getBuffer(currentType);
		float delta = frame.worldDelta();
		double camX = frame.cameraX();
		double camY = frame.cameraY();
		double camZ = frame.cameraZ();

		collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
			var tempNormal = new Vector3f();

			for (var state : states) {
				pose.pushPose();
				pose.translate(
					(float) (state.x - camX),
					(float) (state.y - camY),
					(float) (state.z - camZ)
				);

				state.shape.render(mc, consumer, pose, tempNormal, state.red, state.green, state.blue, state.alpha, state.lightU, state.lightV);
				pose.popPose();
			}
		});
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
