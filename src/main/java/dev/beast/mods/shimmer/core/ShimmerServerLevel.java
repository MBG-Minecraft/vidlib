package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationPayload;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.sound.SoundPayload;
import dev.beast.mods.shimmer.feature.sound.TrackingSoundPayload;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ShimmerServerLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return ((ServerLevel) this).getServer();
	}

	default void shimmer$setActiveZones(ActiveZones zones) {
	}

	@Override
	default int bulkModify(BulkLevelModification modification) {
		var optimized = modification.optimize();

		if (optimized instanceof BulkLevelModificationBundle bundle) {
			var builder = new OptimizedModificationBuilder();

			for (var m : bundle.list()) {
				m.apply(builder);
			}

			optimized = builder.build();
		}

		s2c(new BulkLevelModificationPayload(optimized, ((Level) this).getGameTime()));
		return ShimmerLevel.super.bulkModify(optimized);
	}

	@Override
	default void playSound(Vec3 pos, SoundData sound) {
		s2c(new SoundPayload(pos, sound, ((Level) this).getGameTime()));
	}

	@Override
	default void playTrackingSound(WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) {
		s2c(new TrackingSoundPayload(position, variables, data, looping, ((Level) this).getGameTime()));
	}
}
