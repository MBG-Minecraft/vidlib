package dev.latvian.mods.vidlib.feature.imgui.node;

public class NodePinInfo {
	public final Node node;
	public final NodePin pin;
	public int id;
	public NodePinInfo link;

	public NodePinInfo(Node node, NodePin pin) {
		this.node = node;
		this.pin = pin;
		this.id = 0;
		this.link = null;
	}

	@Override
	public String toString() {
		return "#%,d %s".formatted(id, pin.label());
	}
}
