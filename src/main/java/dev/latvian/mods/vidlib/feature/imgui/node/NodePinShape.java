package dev.latvian.mods.vidlib.feature.imgui.node;

import imgui.extension.imnodes.flag.ImNodesPinShape;

public enum NodePinShape {
	CIRCLE(ImNodesPinShape.Circle),
	FILLED_CIRCLE(ImNodesPinShape.CircleFilled),
	TRIANGLE(ImNodesPinShape.Triangle),
	FILLED_TRIANGLE(ImNodesPinShape.TriangleFilled),
	SQUARE(ImNodesPinShape.Quad),
	FILLED_SQUARE(ImNodesPinShape.QuadFilled);

	public final int id;

	NodePinShape(int id) {
		this.id = id;
	}
}
