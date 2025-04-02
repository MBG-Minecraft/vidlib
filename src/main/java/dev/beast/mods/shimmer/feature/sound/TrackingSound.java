package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.level.Level;

public class TrackingSound extends AbstractTickableSoundInstance {
	public final Level level;
	public final WorldPosition position;
	public final WorldNumberVariables variables;
	public final float targetVolume;

	public TrackingSound(Level level, WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) {
		super(data.sound().value(), data.source(), SoundInstance.createUnseededRandom());
		this.level = level;
		this.position = position;
		this.variables = variables;
		this.looping = looping;
		this.delay = 0;
		this.targetVolume = this.volume = data.volume();
		this.pitch = data.pitch();
		var pos = position.get(new WorldNumberContext(level, 1F, variables));

		if (pos != null) {
			this.x = pos.x;
			this.y = pos.y;
			this.z = pos.z;
		}
	}

	@Override
	public void tick() {
		volume = level.shimmer$getEnvironment().getPauseType().tick() ? targetVolume : 0F;

		var pos = position.get(new WorldNumberContext(level, 1F, variables));

		if (pos != null) {
			x = pos.x;
			y = pos.y;
			z = pos.z;
		} else {
			stop();
		}
	}
}
