package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.particle.VidLibClientParticles;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AutoInit(AutoInit.Type.CLIENT_LOADED)
public class PhysicsParticleManager {
	public static final PhysicsParticleManager CUTOUT_MIPPED = new PhysicsParticleManager("Cutout Mipped", VidLibClientParticles.Physics.CUTOUT_MIPPED, true);
	public static final PhysicsParticleManager TRANSLUCENT = new PhysicsParticleManager("Translucent", VidLibClientParticles.Physics.TRANSLUCENT, true);
	public static final PhysicsParticleManager TRIPWIRE = new PhysicsParticleManager("Tripwire", VidLibClientParticles.Physics.TRIPWIRE, true);
	public static final PhysicsParticleManager CUTOUT = new PhysicsParticleManager("Cutout", VidLibClientParticles.Physics.CUTOUT, false);
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

	public static List<Pair<PhysicsParticleManager, List<PhysicsParticleRenderState>>> extractAll(Minecraft mc, FrustumCheck frustum, Vec3 cameraPos, float delta) {
		var all = List.<Pair<PhysicsParticleManager, List<PhysicsParticleRenderState>>>of();
		var list = new ArrayList<PhysicsParticleRenderState>();

		for (var manager : ALL) {
			manager.rendered = 0;

			if (!manager.particles.isEmpty() && VISIBLE.get()) {
				manager.extract(mc, frustum, cameraPos, delta, list);

				if (!list.isEmpty()) {
					if (all.isEmpty()) {
						all = new ArrayList<>();
					}

					all.add(Pair.of(manager, List.copyOf(list)));
					list.clear();
				}
			}
		}

		return all;
	}

	private static Transparency vl$transparency(BlockState state) {
		var mc = Minecraft.getInstance();
		var sprite = mc.getModelManager().getBlockStateModelSet().get(state).particleMaterial().sprite();
		return sprite == null ? Transparency.NONE : sprite.transparency();
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

	public void extract(Minecraft mc, FrustumCheck frustum, Vec3 cameraPos, float delta, List<PhysicsParticleRenderState> states) {
		var mutablePos = new BlockPos.MutableBlockPos();

		var lightValues = new Long2IntOpenHashMap();
		Long2IntFunction lightComputeFunction = _ -> BlockUtils.getPackedLight(mc.level, mutablePos);

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

			var state = p.renderState;

			if (state == null) {
				state = new PhysicsParticleRenderState();
				p.renderState = state;
			}

			state.x = x;
			state.y = y;
			state.z = z;
			state.scale = dScale;

			state.rotation.identity();

			if (p.rotationAngle != 0F) {
				state.rotation.rotateY(p.rotationAngle);
			}

			float dSpin = KMath.lerp(delta, p.prevSpin, p.spin);

			if (dSpin != 0F) {
				state.rotation.rotateX(dSpin);
			}

			if (p.rotationRoll != 0F) {
				state.rotation.rotateY(p.rotationRoll);
			}

			int light = LightCoordsUtil.FULL_BRIGHT;

			mutablePos.set(p.x, p.y, p.z);

			if (mc.level != null) {
				light = lightValues.computeIfAbsent(mutablePos.asLong(), lightComputeFunction);
			}

			state.shape = p.shape;
			state.red = p.red;
			state.green = p.green;
			state.blue = p.blue;
			state.alpha = p.alpha;
			state.lightU = light & 0xFFFF;
			state.lightV = (light >> 16) & 0xFFFF;
			rendered++;

			states.add(state);
		}
	}

	public void submit(SubmitContext ctx, List<PhysicsParticleRenderState> states) {
		ctx.submit(renderType, (pose, buffer) -> {
			var tempNormal = new Vector3f();
			var original = pose.copy();

			for (var state : states) {
				pose.translate(
					(float) (state.x - ctx.cameraPos().x),
					(float) (state.y - ctx.cameraPos().y),
					(float) (state.z - ctx.cameraPos().z)
				);

				pose.rotate(state.rotation);

				if (state.scale != 1F) {
					pose.scale(state.scale, state.scale, state.scale);
				}

				state.shape.render(
					ctx.mc(),
					buffer,
					pose,
					tempNormal,
					state.red,
					state.green,
					state.blue,
					state.alpha,
					state.lightU,
					state.lightV
				);

				pose.set(original);
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
