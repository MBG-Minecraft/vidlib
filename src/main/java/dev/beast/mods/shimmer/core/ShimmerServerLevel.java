package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationPayload;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticlesIdPayload;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticlesPayload;
import dev.beast.mods.shimmer.feature.prop.ServerPropList;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.sound.SoundPayload;
import dev.beast.mods.shimmer.feature.sound.TrackingSoundPayload;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ShimmerServerLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return shimmer$level().getServer();
	}

	@Override
	default ServerLevel shimmer$level() {
		return (ServerLevel) this;
	}

	@Override
	default ServerPropList getProps() {
		throw new NoMixinException(this);
	}

	default void shimmer$setActiveZones(ActiveZones zones) {
	}

	@Override
	@Nullable
	default Entity getEntityByUUID(UUID uuid) {
		return shimmer$level().getEntity(uuid);
	}

	@Override
	default int bulkModify(BulkLevelModification modification) {
		var optimized = modification.optimize();

		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		if (optimized instanceof BulkLevelModificationBundle bundle) {
			var builder = new OptimizedModificationBuilder();

			for (var m : bundle.list()) {
				m.apply(builder);
			}

			optimized = builder.build();
		}

		s2c(new BulkLevelModificationPayload(optimized));
		return ShimmerLevel.super.bulkModify(optimized);
	}

	@Override
	default void playSound(Vec3 pos, SoundData sound) {
		s2c(new SoundPayload(pos, sound));
	}

	@Override
	default void playTrackingSound(WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) {
		s2c(new TrackingSoundPayload(position, variables, data, looping));
	}

	@Override
	default void physicsParticles(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) {
		if (!blocks.isEmpty()) {
			s2c(new PhysicsParticlesPayload(data, seed, blocks));
		}
	}

	@Override
	default void physicsParticles(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
		if (!blocks.isEmpty()) {
			s2c(new PhysicsParticlesIdPayload(id, seed, blocks));
		}
	}

	default boolean shimmer$cancelWrite() {
		throw new NoMixinException(this);
	}

	default void shimmer$reloadChunks() {
		throw new NoMixinException(this);
	}
}
