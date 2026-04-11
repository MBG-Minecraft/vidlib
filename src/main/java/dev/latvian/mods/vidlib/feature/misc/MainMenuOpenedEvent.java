package dev.latvian.mods.vidlib.feature.misc;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.Event;

public class MainMenuOpenedEvent extends Event {
	private final Minecraft minecraft;
	private final boolean firstTime;

	public MainMenuOpenedEvent(Minecraft minecraft, boolean firstTime) {
		this.minecraft = minecraft;
		this.firstTime = firstTime;
	}

	public Minecraft getMinecraft() {
		return minecraft;
	}

	public boolean isFirstTime() {
		return firstTime;
	}
}
