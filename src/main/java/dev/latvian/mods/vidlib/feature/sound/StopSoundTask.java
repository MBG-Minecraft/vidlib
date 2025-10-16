package dev.latvian.mods.vidlib.feature.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;

public record StopSoundTask(Object parent, SoundInstance instance) implements Runnable {
	@Override
	public void run() {
		Minecraft.getInstance().getSoundManager().stop(instance);
	}
}
