package dev.latvian.mods.vidlib.feature.capture;

import com.google.gson.JsonObject;
import net.neoforged.bus.api.Event;

public abstract class PacketCaptureEvent extends Event {
	private final PacketCapture packetCapture;

	public PacketCaptureEvent(PacketCapture packetCapture) {
		this.packetCapture = packetCapture;
	}

	public PacketCapture getPacketCapture() {
		return packetCapture;
	}

	public static class Finished extends PacketCaptureEvent {
		public Finished(PacketCapture packetCapture) {
			super(packetCapture);
		}
	}

	public static class Metadata extends PacketCaptureEvent {
		private final JsonObject metadata;

		public Metadata(PacketCapture packetCapture, JsonObject metadata) {
			super(packetCapture);
			this.metadata = metadata;
		}

		public JsonObject getMetadata() {
			return metadata;
		}
	}
}
