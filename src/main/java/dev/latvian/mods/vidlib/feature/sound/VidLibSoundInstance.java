package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.level.Level;

public class VidLibSoundInstance extends AbstractTickableSoundInstance {
	public final Level level;
	public final KVector position;
	public final KNumberContext numberContext;
	public final float targetVolume;
	public final boolean stopImmediately;

	public VidLibSoundInstance(Level level, PositionedSoundData data, KNumberVariables variables) {
		super(data.data().sound().value(), data.data().source(), SoundInstance.createUnseededRandom());
		this.level = level;
		this.position = data.position().orElse(null);
		this.numberContext = level.getGlobalContext().fork(1F, variables);
		this.looping = data.looping();
		this.stopImmediately = data.stopImmediately();
		this.delay = 0;
		this.targetVolume = this.volume = data.data().volume();
		this.pitch = data.data().pitch();
		var pos = position == null ? null : position.get(numberContext);

		if (pos != null) {
			this.x = pos.x;
			this.y = pos.y;
			this.z = pos.z;
		}
	}

	@Override
	public void tick() {
		volume = level.getEnvironment().getPauseType().tick() ? targetVolume : 0F;

		var pos = position == null ? null : position.get(numberContext);

		if (pos != null) {
			x = pos.x;
			y = pos.y;
			z = pos.z;
		} else if (stopImmediately) {
			stop();
		}
	}
}
