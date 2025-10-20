package dev.latvian.mods.vidlib.feature.imgui.node;

public enum NodePinConnectionType {
	OUTPUT("output"),
	REQUIRED_INPUT("required_input"),
	OPTIONAL_INPUT("optional_input");

	public final String name;

	NodePinConnectionType(String name) {
		this.name = name;
	}
}
