package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticles;
import dev.beast.mods.shimmer.feature.prop.ClientPropList;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.sound.TrackingSound;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface ShimmerClientLevel extends ShimmerLevel, ShimmerClientEntityContainer {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default ClientLevel shimmer$level() {
		return (ClientLevel) this;
	}

	@Override
	default ClientPropList getProps() {
		throw new NoMixinException(this);
	}

	@Override
	@Nullable
	default ActiveZones shimmer$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.shimmer$sessionData().filteredZones;
	}

	@Override
	default void redrawSection(int sectionX, int sectionY, int sectionZ, boolean mainThread) {
		Minecraft.getInstance().levelRenderer.setSectionDirty(sectionX, sectionY, sectionZ, mainThread);
	}

	@Override
	default void playSound(Vec3 pos, SoundData sound) {
		var mc = Minecraft.getInstance();
		mc.getSoundManager().play(new SimpleSoundInstance(sound.sound().value(), sound.source(), sound.volume(), sound.pitch(), ((Level) this).random, pos.x, pos.y, pos.z));
	}

	@Override
	default void playTrackingSound(WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) {
		Minecraft.getInstance().getSoundManager().play(new TrackingSound((Level) this, position, variables, data, looping));
	}

	@Override
	default void physicsParticles(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		var particles = new PhysicsParticles(data, shimmer$level(), shimmer$level().getGameTime(), seed);

		for (var block : blocks) {
			particles.at = block.pos();
			particles.state = block.state();
			particles.spawn();
		}
	}

	@Override
	default void physicsParticles(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		var data = PhysicsParticleData.REGISTRY.get(id);
		physicsParticles(data == null ? PhysicsParticleData.DEFAULT : data, seed, blocks);
	}

	default void environmentEffects(Minecraft mc, BlockPos pos) {
		var override = EntityOverride.ENVIRONMENT_EFFECTS.get(mc.player);
		var level = shimmer$level();

		if (override != null && !override.isEmpty()) {
			for (var effect : override) {
				if (level.random.nextFloat() <= effect.chance()) {
					level.addParticle(
						effect.particle(),
						pos.getX() + level.random.nextDouble(),
						pos.getY() + level.random.nextDouble(),
						pos.getZ() + level.random.nextDouble(),
						0.0, 0.0, 0.0
					);
				}
			}
		}
	}

	@Override
	default void spawnCubeParticles(Map<CubeParticleOptions, List<BlockPos>> map) {
		for (var entry : map.entrySet()) {
			for (var pos : entry.getValue()) {
				shimmer$level().addParticle(entry.getKey(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			}
		}
	}
}
