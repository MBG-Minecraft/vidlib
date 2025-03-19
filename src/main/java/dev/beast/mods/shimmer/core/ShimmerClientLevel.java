package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.sound.TrackingSound;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ShimmerClientLevel extends ShimmerLevel, ShimmerClientEntityContainer {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return Minecraft.getInstance();
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
}
