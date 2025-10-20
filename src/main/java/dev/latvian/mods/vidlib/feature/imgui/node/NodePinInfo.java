package dev.latvian.mods.vidlib.feature.imgui.node;

public class NodePinInfo {
	public final Node node;
	public final NodePin pin;
	public int id;
	public NodePinInfo inputLink;
	public boolean inputLinkSelected;

	public NodePinInfo(Node node, NodePin pin) {
		this.node = node;
		this.pin = pin;
		this.id = 0;
		this.inputLink = null;
	}

	@Override
	public String toString() {
		return "#%,d %s [%s, %s]".formatted(id, pin.label(), pin.type().displayName, pin.connectionType().name);
	}
}
