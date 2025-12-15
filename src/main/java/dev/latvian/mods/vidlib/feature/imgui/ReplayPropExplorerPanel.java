package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.RecordedProp;
import dev.latvian.mods.vidlib.integration.FlashbackIntegration;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.ArrayList;

public class ReplayPropExplorerPanel extends AdminPanel {
	public static final ReplayPropExplorerPanel INSTANCE = new ReplayPropExplorerPanel();

	public ReplayPropExplorerPanel() {
		super("replay-prop-explorer", "Replay Prop Explorer");
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.isReplay) {
			close();
			return;
		}

		if (RecordedProp.LIST == null) {
			return;
		}

		ImGui.text("WIP!");

		var allProps = new ArrayList<>(RecordedProp.LIST);
		var replayStart = FlashbackIntegration.getStartTick();
		var replayEnd = FlashbackIntegration.getEndTick();

		ImGui.text("Replay Props: %,d".formatted(allProps.size()));

		if (graphics.button(ImIcons.ADD + " Add##add", ImColorVariant.GREEN)) {
			ImGui.openPopup("###add-replay-prop-popup");
		}

		if (ImGui.beginPopup("###add-replay-prop-popup")) {
			ImGui.text("WIP!");
			ImGui.endPopup();
		}

		ImGui.pushItemWidth(-1F);

		for (var prop : allProps) {
			ImGui.pushID(prop.id);

			if (graphics.collapsingHeader("%s#%08X###header".formatted(prop.type.id(), prop.id), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
				ClientProps.OPEN_PROPS.add(prop.id);

				ImGuiUtils.INT.set((int) (prop.spawn - replayStart));
				ImGuiUtils.INT1_2.set((int) (prop.remove - replayStart));

				if (ImGui.dragIntRange2("###range", ImGuiUtils.INT.getData(), ImGuiUtils.INT1_2.getData(), 1F, 0, (int) (replayEnd - replayStart))) {
					prop.spawn = ImGuiUtils.INT.get() + replayStart;
					prop.remove = ImGuiUtils.INT1_2.get() + replayStart;

					if (prop.remove <= prop.spawn) {
						prop.remove = prop.spawn + 1L;
					}
				}
			}

			if (ImGui.isItemHovered()) {
				ClientProps.OPEN_PROPS.add(prop.id);
			}

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}
}
