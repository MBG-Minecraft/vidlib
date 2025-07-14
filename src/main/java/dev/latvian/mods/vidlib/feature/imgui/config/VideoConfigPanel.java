package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VideoConfigPanel extends AdminPanel {
	public final Supplier<List<ConfigEntryList>> config;
	private final Consumer<ImGraphics> extraContent;

	public VideoConfigPanel(String label, Supplier<List<ConfigEntryList>> config, @Nullable Consumer<ImGraphics> extraContent) {
		super("video-config", label);
		this.config = config;
		this.extraContent = extraContent;
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.isAdmin) {
			close();
			return;
		}

		var mc = Minecraft.getInstance();

		if (extraContent != null) {
			extraContent.accept(graphics);
		}

		var allLists = config.get();
		boolean allSame = true;

		for (var all : allLists) {
			if (!all.isDefault()) {
				allSame = false;
				break;
			}
		}

		if (allSame) {
			ImGui.beginDisabled();
		}

		ImGui.button("Reset All###reset-all");

		if (allSame) {
			ImGui.endDisabled();
		}

		if (ImGui.isItemClicked()) {
			for (var all : allLists) {
				all.reset(graphics);
			}
		}

		ImGui.pushItemWidth(-1F);

		for (var all : allLists) {
			ImGui.spacing();
			ImGui.spacing();

			if (ImGui.collapsingHeader(all.label().getString(), ImGuiTreeNodeFlags.DefaultOpen)) {
				for (var config : all.config()) {
					ImGui.spacing();

					config.init(mc.level.getServerData());
					var update = config.imgui(graphics);

					if (update.isAny()) {
						config.update(graphics, update.isFull());
					}
				}
			}
		}

		ImGui.popItemWidth();
	}
}
