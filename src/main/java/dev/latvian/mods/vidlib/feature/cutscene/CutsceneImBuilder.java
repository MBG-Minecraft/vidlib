package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImIcons;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CutsceneImBuilder implements ImBuilder<Cutscene> {
	public final WorldNumberVariables variables;
	public final List<CutsceneStepImBuilder> steps = new ArrayList<>();
	public final ImBoolean allowMovement = new ImBoolean(false);
	public final ImBoolean openPreviousScreen = new ImBoolean(false);
	public final ImBoolean hidePlayer = new ImBoolean(false);

	public CutsceneImBuilder() {
		this.variables = new WorldNumberVariables();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var ctx = Minecraft.getInstance().level.globalContext(0F).withVariables(variables);
		var update = ImUpdate.NONE;

		ImGui.columns(2);

		if (ImGui.button("Preview###preview-cutscene", -1F, 0F)) {
			if (isValid()) {
				var cutscene = build();
				Minecraft.getInstance().playCutscene(cutscene, variables);
			} else {
				Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("AAA"), null));
			}
		}

		ImGui.nextColumn();

		if (ImGui.button("Copy JSON###copy-cutscene-json", -1F, 0F)) {
		}

		ImGui.columns();

		ImGui.separator();

		update = update.or(ImGui.checkbox("Allow Movement###allow-movement", allowMovement));
		update = update.or(ImGui.checkbox("Open Previous Screen###open-previous-screen", openPreviousScreen));
		update = update.or(ImGui.checkbox("Hide Player###hide-player", hidePlayer));

		for (int i = 0; i < steps.size(); i++) {
			var step = steps.get(i);
			var start = Optional.ofNullable(step.start.build().get(ctx)).orElse(0D).intValue();
			var length = Optional.ofNullable(step.length.build().get(ctx)).orElse(1D).intValue();

			ImGui.pushID(i);
			ImGuiUtils.BOOLEAN.set(true);

			if (ImGui.collapsingHeader(length > 1 ? ("Step @ " + start / 20F + "s for " + length / 20F + "s") : ("Step @ " + start / 20F + "s"), ImGuiUtils.BOOLEAN, ImGuiTreeNodeFlags.DefaultOpen)) {
				update = update.or(step.imgui(graphics));

				if (!ImGuiUtils.BOOLEAN.get()) {
					step.delete = true;
				}
			}

			ImGui.popID();
		}

		if (ImGui.button(ImIcons.ADD + " Step###add-step")) {
			steps.add(new CutsceneStepImBuilder(this));
		}

		return update;
	}

	@Override
	public boolean isValid() {
		for (var step : steps) {
			if (!step.isValid()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Cutscene build() {
		var list = new ArrayList<CutsceneStep>(steps.size());

		for (var step : steps) {
			list.add(step.build());
		}

		return new Cutscene(list, allowMovement.get(), openPreviousScreen.get(), hidePlayer.get());
	}
}
