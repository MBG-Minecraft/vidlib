package dev.latvian.mods.vidlib.feature.imgui.node;

public record NodePin(NodePinType<?> type, String label, NodePinConnectionType connectionType, NodePinShape shape) {
	public NodePin withShape(NodePinShape shape) {
		return new NodePin(type, label, connectionType, shape);
	}
}
