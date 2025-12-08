package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

public class PacketDebuggerPanel extends AdminPanel {
	public record LoggedPacket(long uid, long remoteGameTime, SimplePacketPayload payload) {
	}

	public static final PacketDebuggerPanel INSTANCE = new PacketDebuggerPanel();

	public final List<LoggedPacket> debugPackets;
	public final ImBoolean includeHidden;

	public PacketDebuggerPanel() {
		super("packet-debugger", "Packet Debugger");
		this.debugPackets = new ArrayList<>();
		this.includeHidden = new ImBoolean(false);
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			ImGui.text("No packets");
			return;
		}

		ImGui.pushItemWidth(-1F);
		ImGui.checkbox("Include Hidden Packets", includeHidden);

		for (int i = 0; i < debugPackets.size(); i++) {
			var p = debugPackets.get(i);

			if (p.payload.allowDebugLogging() || includeHidden.get()) {
				if (graphics.collapsingHeader(p.payload.getType().type().id() + "###packet-" + i, 0)) {
					ImGui.textWrapped(p.toString());
				}
			}
		}

		ImGui.popItemWidth();
	}
}
