package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;

import java.util.function.Consumer;

public class ReplayCaptureConfigSnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final Consumer<Packet<? super ClientConfigurationPacketListener>> callback;

	public ReplayCaptureConfigSnapshotEvent(ReplayAPI api, ReplayCaptureSession session, Consumer<Packet<? super ClientConfigurationPacketListener>> callback) {
		super(api, session);
		this.callback = callback;
	}

	public void s2c(Packet<? super ClientConfigurationPacketListener> packet) {
		callback.accept(packet);
	}
}
