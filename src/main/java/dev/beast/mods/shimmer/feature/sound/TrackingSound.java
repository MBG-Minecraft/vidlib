package dev.beast.mods.shimmer.feature.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;

public class TrackingSound extends AbstractTickableSoundInstance {
	public final Entity entity;

	public TrackingSound(Entity entity, SoundData data, boolean looping) {
		super(data.sound().value(), data.source(), SoundInstance.createUnseededRandom());
		this.entity = entity;
		this.looping = looping;
		this.delay = 0;
		this.volume = data.volume();
		this.pitch = data.pitch();
		this.x = entity.getX();
		this.y = entity.getY();
		this.z = entity.getZ();
	}

	@Override
	public void tick() {
		if (entity.isRemoved()) {
			stop();
			return;
		}

		x = entity.getX();
		y = entity.getY();
		z = entity.getZ();
	}
}
