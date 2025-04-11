package dev.beast.mods.shimmer.feature.block;

import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleShape;
import dev.beast.mods.shimmer.util.WithCache;
import dev.latvian.mods.kmath.SplitBox;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.level.block.state.BlockState;

public class ShimmerBlockStateClientProperties implements WithCache {
	public static ShimmerBlockStateClientProperties of(BlockState state) {
		return (ShimmerBlockStateClientProperties) state.shimmer$clientProperties();
	}

	public final BlockState state;
	private Int2ObjectMap<PhysicsParticleShape> blockParticleShapes;
	public PhysicsParticleManager manager;

	public ShimmerBlockStateClientProperties(BlockState state) {
		this.state = state;
	}

	@Override
	public void clearCache() {
		if (blockParticleShapes != null) {
			for (var v : blockParticleShapes.values()) {
				v.clearCache();
			}
		}

		blockParticleShapes = null;
		manager = null;
	}

	public PhysicsParticleShape getPhysicsBlockParticleShape(SplitBox box) {
		if (blockParticleShapes == null) {
			blockParticleShapes = new Int2ObjectOpenHashMap<>();
		}

		var key = box.key();
		var e = blockParticleShapes.get(key);

		if (e == null) {
			e = new PhysicsParticleShape(state, box);
			blockParticleShapes.put(key, e);
		}

		return e;
	}

	public PhysicsParticleManager getManager() {
		if (manager == null) {
			manager = PhysicsParticleManager.of(state);
		}

		return manager;
	}
}
