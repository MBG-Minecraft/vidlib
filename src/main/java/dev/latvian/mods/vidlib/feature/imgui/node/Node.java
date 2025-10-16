package dev.latvian.mods.vidlib.feature.imgui.node;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;

import java.util.ArrayList;
import java.util.List;

public class Node {
	public int id;
	public final ImBuilder<?> builder;
	public final List<NodePinInfo> inputPins;
	public final List<NodePinInfo> outputPins;

	public Node(ImBuilder<?> builder, List<NodePin> pins) {
		this.builder = builder;
		this.inputPins = new ArrayList<>(1);
		this.outputPins = new ArrayList<>(1);

		for (var pin : pins) {
			var pinInfo = new NodePinInfo(this, pin);

			if (pin.connectionType() == NodePinConnectionType.OUTPUT) {
				outputPins.add(pinInfo);
			} else {
				inputPins.add(pinInfo);
			}
		}
	}
}
