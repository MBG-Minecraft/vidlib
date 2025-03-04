package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClockInstancePayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import net.minecraft.world.level.Level;

public class ShimmerSessionData {
	private CookieMap cookies;

	public CookieMap getCookies() {
		if (cookies == null) {
			cookies = new CookieMap();
		}

		return cookies;
	}

	public void updateZones(Level level, SyncZonesPayload payload) {
	}

	public void updateClockFonts(SyncClockFontsPayload payload) {
	}

	public void updateClocks(Level level, SyncClocksPayload payload) {
	}

	public void updateClockInstance(SyncClockInstancePayload payload) {
	}

	public void respawned(Level level, boolean loggedIn) {
	}

	public void closed() {
	}
}
