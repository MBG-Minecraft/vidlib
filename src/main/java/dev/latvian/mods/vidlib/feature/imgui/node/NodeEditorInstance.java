package dev.latvian.mods.vidlib.feature.imgui.node;

import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

public class NodeEditorInstance<T> {
	public final NodePinType<T> type;
	public final Node root;
	public final Int2ObjectMap<Node> nodes;
	public final Int2ObjectMap<NodePinInfo> pins;
	private final ImInt tempSrc;
	private final ImInt tempDst;
	private NodePinInfo lastDroppedPin;
	public float miniMap;
	public int lastId;

	public NodeEditorInstance(NodePinType<T> type) {
		this.type = type;
		this.root = new Node(null, type.singleRequiredInput);
		this.nodes = new Int2ObjectLinkedOpenHashMap<>();
		this.pins = new Int2ObjectLinkedOpenHashMap<>();
		this.tempSrc = new ImInt();
		this.tempDst = new ImInt();
		this.miniMap = 0.1F;
		this.lastId = 0;

		addNode(root);
	}

	public int nextId() {
		return ++lastId;
	}

	public void addNode(Node node) {
		if (node.id == 0) {
			node.id = nextId();
		}

		nodes.put(node.id, node);

		for (var pin : node.inputPins) {
			if (pin.id == 0) {
				pin.id = nextId();
			}

			pins.put(pin.id, pin);
		}

		for (var pin : node.outputPins) {
			if (pin.id == 0) {
				pin.id = nextId();
			}

			pins.put(pin.id, pin);
		}
	}

	private void dropNewNode(@Nullable Node node) {
		if (node == null || node.inputPins.isEmpty() && node.outputPins.isEmpty()) {
			return;
		}

		if (lastDroppedPin != null) {
			for (var pin : node.outputPins) {
				if (pin.pin.type() == lastDroppedPin.pin.type()) {
					pin.link = lastDroppedPin;
					break;
				}
			}

			lastDroppedPin = null;
		}

		addNode(node);
	}

	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		boolean mouseRightButton = ImGui.isMouseClicked(1);

		ImNodes.beginNodeEditor();
		boolean nodeEditorHovered = ImNodes.isEditorHovered();
		Node removedNode = null;

		for (var node : nodes.values()) {
			ImNodes.beginNode(node.id);
			ImNodes.beginNodeTitleBar();

			if (node.builder != null) {
				if (graphics.iconButton(ImIcons.TRASHCAN, "###delete-node-" + node.id, "Delete", ImColorVariant.RED)) {
					removedNode = node;
				}

				ImGui.sameLine();
			}

			ImGui.text(node.builder == null ? "Root" : node.builder.getDisplayName());
			ImNodes.endNodeTitleBar();

			if (node.builder != null) {
				// ImNodes.beginStaticAttribute(node.id);
				update = update.or(node.builder.nodeImgui(graphics));
				// ImNodes.endStaticAttribute();
			}

			for (var pin : node.inputPins) {
				ImNodes.beginInputAttribute(pin.id);
				ImGui.textUnformatted(pin.pin.label());
				ImNodes.endInputAttribute();
			}

			for (var pin : node.outputPins) {
				ImNodes.beginOutputAttribute(pin.id);
				ImGui.textUnformatted(pin.pin.label());
				ImNodes.endOutputAttribute();
			}

			ImNodes.endNode();

			/*
			ImGui.sameLine();

			graphics.pushStack();
			graphics.setNodesPin(ImColorVariant.YELLOW);
			ImNodes.beginOutputAttribute(2);

			ImGui.indent(200F - ImGui.calcTextSize("Left Side").x - ImGui.calcTextSize("Right Side").x);

			ImGui.textUnformatted("Right Side");
			ImNodes.endInputAttribute();
			graphics.popStack();

			ImNodes.beginStaticAttribute(10);
			ImGui.setNextItemWidth(120F);
			//ImGui.inputText("###node-a-input", stringData);
			ImNodes.endStaticAttribute();
			 */
		}

		for (var pin : pins.values()) {
			if (pin.link != null) {
				ImNodes.link(pin.id, pin.link.id, pin.id);
			}
		}

		if (miniMap > 0F) {
			ImNodes.miniMap(miniMap, ImNodesMiniMapLocation.TopRight);
		}

		ImNodes.endNodeEditor();

		if (ImNodes.isLinkCreated(tempSrc, tempDst)) {
			var src = pins.get(tempSrc.get());
			var dst = pins.get(tempDst.get());
			var in = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? dst : src;
			var out = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? src : dst;

			in.link = out;

			update = ImUpdate.FULL;
		}

		if (ImNodes.isLinkDestroyed(tempSrc)) {
			var pin = pins.get(tempSrc.get());
			update = ImUpdate.FULL;
		}

		if (ImNodes.isLinkDropped(tempSrc, false)) {
			lastDroppedPin = pins.get(tempSrc.get());
			ImGui.openPopup("###context-menu");
		}

		if (nodeEditorHovered && mouseRightButton && !ImGui.isAnyItemHovered()) {
			ImGui.openPopup("###context-menu");
		}

		if (ImGui.beginPopup("###context-menu")) {
			type.menu.apply(this::dropNewNode).build(graphics);
			ImGui.endPopup();
		}

		if (lastDroppedPin != null && !ImGui.isPopupOpen("###context-menu")) {
			lastDroppedPin = null;
		}

		if (removedNode != null) {
			update = ImUpdate.FULL;
		}

		return update;
	}

	public void clear() {
		nodes.clear();
		pins.clear();

		for (var pin : root.inputPins) {
			pin.link = null;
		}

		addNode(root);
	}
}
