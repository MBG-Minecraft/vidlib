package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplayConfigPacket;
import dev.latvian.mods.replay.api.ReplayGamePacket;
import dev.latvian.mods.replay.api.ReplayMarker;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.replay.api.ReplayMarkerType;
import dev.latvian.mods.replay.api.ReplaySession;

import java.util.List;
import java.util.function.Consumer;

public class ReplaySessionOpenedEvent extends ReplaySessionEvent {
	private final List<ReplayConfigPacket> configPackets;
	private final List<ReplayGamePacket> gamePackets;
	private final Consumer<ReplayMarker> markerCallback;

	public ReplaySessionOpenedEvent(
		ReplayAPI api,
		ReplaySession session,
		List<ReplayConfigPacket> configPackets,
		List<ReplayGamePacket> gamePackets,
		Consumer<ReplayMarker> markerCallback
	) {
		super(api, session);
		this.configPackets = configPackets;
		this.gamePackets = gamePackets;
		this.markerCallback = markerCallback;
	}

	public List<ReplayConfigPacket> getConfigPackets() {
		return configPackets;
	}

	public List<ReplayGamePacket> getGamePackets() {
		return gamePackets;
	}

	public void addMarker(int time, ReplayMarkerType type, ReplayMarkerData data) {
		markerCallback.accept(new ReplayMarker(time, type, data));
	}

	public void addMarker(int time, ReplayMarkerData data) {
		addMarker(time, ReplayMarkerType.POST_RECORDING, data);
	}
}
