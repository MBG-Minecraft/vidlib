package dev.latvian.mods.vidlib.feature.block;

import dev.latvian.mods.klib.math.SplitBox;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.core.VLExtensions;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleShape;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.block.GrassBlock;
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

	public final BlockState state;
	private Int2ObjectMap<PhysicsParticleShape> blockParticleShapes;
	public PhysicsParticleManager manager;
	private UV baseUV;

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
		baseUV = null;
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

	public UV getBaseUV() {
		if (baseUV == null) {
			TextureAtlasSprite sprite;

			if (state.getBlock() instanceof GrassBlock) {
				sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(ID.vidlib("block/grass"));
			} else {
				sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).particleMaterial().sprite();
			}

			return new UV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
		}

		return baseUV;
	}
}
