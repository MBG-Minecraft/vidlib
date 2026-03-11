package dev.latvian.mods.replay.api;

import dev.latvian.mods.vidlib.VidLib;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ReplayAPI {
	private static final ReplayAPI NONE = new ReplayAPI("none", "0");
	private static final Mutable<ReplayAPI> ACTIVE = new MutableObject<>(NONE);

	public static boolean isLoaded() {
		return ACTIVE.getValue() != NONE;
	}

	public static ReplayAPI getActive() {
		return ACTIVE.getValue();
	}

	public static void setActive(ReplayAPI api) {
		ACTIVE.setValue(api);
		VidLib.LOGGER.info("Loaded Replay API " + api);
	}

	public final String name;
	public final String version;

	public ReplayAPI(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public String toString() {
		if (this == NONE) {
			return "none";
		}

		return name + "/" + version + " (" + getClass().getName() + ")";
	}

	@Nullable
	public ReplaySession getOpenSession() {
		return null;
	}

	public boolean isInReplay() {
		return getOpenSession() != null;
	}

	public boolean isExporting() {
		return false;
	}

	public boolean isInReplayOrExporting() {
		return isInReplay() || isExporting();
	}

	public boolean getRenderBlocks() {
		return true;
	}

	public boolean getRenderEntities() {
		return true;
	}

	public boolean getRenderPlayers() {
		return true;
	}

	public boolean getRenderParticles() {
		return true;
	}

	public boolean getRenderNameTags() {
		return true;
	}

	public boolean isEntityHidden(UUID uuid) {
		return false;
	}

	public boolean isNameHidden(UUID uuid) {
		return false;
	}

	public boolean isHealthHidden(UUID uuid) {
		return false;
	}
}
