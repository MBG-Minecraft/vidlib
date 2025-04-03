package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.level.Level;

public class ShimmerSoundInstance extends AbstractTickableSoundInstance {
	public final Level level;
	public final WorldPosition position;
	public final WorldNumberVariables variables;
	public final float targetVolume;
	public final boolean stopImmediately;

	public ShimmerSoundInstance(Level level, PositionedSoundData data, WorldNumberVariables variables) {
		super(data.data().sound().value(), data.data().source(), SoundInstance.createUnseededRandom());
		this.level = level;
		this.position = data.position().orElse(null);
		this.variables = variables;
		this.looping = data.looping();
		this.stopImmediately = data.stopImmediately();
		this.delay = 0;
		this.targetVolume = this.volume = data.data().volume();
		this.pitch = data.data().pitch();
		var pos = position == null ? null : position.get(new WorldNumberContext(level, 1F, variables));

		if (pos != null) {
			this.x = pos.x;
			this.y = pos.y;
			this.z = pos.z;
		}
	}

	@Override
	public void tick() {
		volume = level.shimmer$getEnvironment().getPauseType().tick() ? targetVolume : 0F;

		var pos = position == null ? null : position.get(new WorldNumberContext(level, 1F, variables));

		if (pos != null) {
			x = pos.x;
			y = pos.y;
			z = pos.z;
		} else if (stopImmediately) {
			stop();
		}
	}
}
