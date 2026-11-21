package dev.latvian.mods.vidlib.feature.block;

import dev.latvian.mods.klib.math.SplitBox;
import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.core.VLExtensions;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleShape;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class VidLibBlockStateClientProperties implements WithCache {
	private static final Function<BlockState, VidLibBlockStateClientProperties> FACTORY = VidLibBlockStateClientProperties::new;

	public static VidLibBlockStateClientProperties of(BlockState state) {
		return VLExtensions.compute(state, 0, FACTORY);
	}

	public static void clearAllCache() {
		for (var block : BuiltInRegistries.BLOCK) {
			for (var state : block.getStateDefinition().getPossibleStates()) {
				if (VLExtensions.get(state, 0) instanceof WithCache withCache) {
					withCache.clearCache();
				}

				VLExtensions.set(state, 0, null);
			}
		}
	}

	private Int2ObjectMap<PhysicsParticleShape> blockParticleShapes;
	public PhysicsParticleManager manager;
	public final BlockState state;

	public VidLibBlockStateClientProperties(BlockState state) {
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
