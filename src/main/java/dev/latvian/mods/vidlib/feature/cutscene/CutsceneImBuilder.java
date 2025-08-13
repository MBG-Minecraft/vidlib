package dev.latvian.mods.vidlib.feature.cutscene;

import com.google.gson.JsonElement;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.util.JsonUtils;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CutsceneImBuilder implements ImBuilder<Cutscene> {
	public final KNumberVariables variables;
	public KNumberContext numberContext;
	public final List<CutsceneStepImBuilder> steps = new ArrayList<>();
	public final ImBoolean allowMovement = new ImBoolean(false);
	public final ImBoolean openPreviousScreen = new ImBoolean(false);
	public final ImBoolean hidePlayer = new ImBoolean(false);
	public Cutscene cutscene = null;
	public JsonElement cutsceneJson = null;
	public final ImString json = ImGuiUtils.resizableString();
	public int jsonLines = 0;

	public CutsceneImBuilder() {
		this.variables = new KNumberVariables();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		numberContext = Minecraft.getInstance().level.getGlobalContext().fork(0F, variables);
		var update = ImUpdate.NONE;

		if (cutscene == null) {
			ImGui.beginDisabled();
		}

		if (ImGui.button("Preview###preview-cutscene", -1F, 0F)) {
			if (cutscene != null) {
				try {
					Minecraft.getInstance().c2s(new PreviewCutscenePayload(cutscene, variables));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		if (cutscene == null) {
			ImGui.endDisabled();
		} else {
			int len = 0;

			for (var steps : cutscene.steps) {
				var start = steps.start();
				var length = Mth.ceil(steps.length().getOr(numberContext, 0D));
				len = Math.max(len, start + length);
			}

			ImGui.text("Total length: " + KMath.format(len / 20F) + "s");
		}

		ImGui.separator();

		update = update.or(ImGui.checkbox(ImIcons.FREEZE + " Allow Movement###allow-movement", allowMovement));
		update = update.or(ImGui.checkbox(ImIcons.UNDO + " Open Previous Screen###open-previous-screen", openPreviousScreen));
		update = update.or(ImGui.checkbox(ImIcons.INVISIBLE + " Hide Player###hide-player", hidePlayer));

		for (int i = 0; i < steps.size(); i++) {
			var step = steps.get(i);
			var start = step.start.get();
			var length = Mth.ceil(Optional.ofNullable(step.length.isValid() ? step.length.build().get(numberContext) : null).orElse(0D));

			ImGui.pushID(i);
			ImGuiUtils.BOOLEAN.set(true);

			if (graphics.collapsingHeader(length >= 1 ? ("Step @ " + KMath.format(start / 20F) + "s for " + KMath.format(length / 20F) + "s") : ("Step @ " + KMath.format(start / 20F) + "s"), ImGuiUtils.BOOLEAN, ImGuiTreeNodeFlags.DefaultOpen)) {
				boolean shouldDelete = !ImGuiUtils.BOOLEAN.get();

				update = update.or(step.imgui(graphics));

				if (shouldDelete) {
					step.delete = true;
				}
			}

			ImGui.popID();
		}

		if (steps.removeIf(step -> step.delete)) {
			update = ImUpdate.FULL;
		}

		ImGui.separator();

		if (ImGui.button(ImIcons.ADD + " Step###add-step")) {
			steps.add(new CutsceneStepImBuilder(this));
			update = ImUpdate.FULL;
		}

		ImGui.separator();

		if (update.isFull()) {
			cutscene = null;
			cutsceneJson = null;
			json.clear();
			jsonLines = 0;

			if (isValid()) {
				try {
					var c = build();
					cutsceneJson = Cutscene.DIRECT_CODEC.encodeStart(Minecraft.getInstance().level.jsonOps(), c).getOrThrow();
					json.set(JsonUtils.prettyString(cutsceneJson));
					cutscene = c;

					for (var ch : json.get().toCharArray()) {
						if (ch == '\n') {
							jsonLines++;
						}
					}
				} catch (Exception ex) {
				}
			}
		}

		ImGui.text("JSON");
		ImGui.sameLine();

		if (cutsceneJson == null) {
			ImGui.beginDisabled();
			ImGui.smallButton("Copy to Clipboard###copy-json");
			ImGui.endDisabled();
		} else if (ImGui.smallButton("Copy to Clipboard###copy-json")) {
			ImGui.setClipboardText(cutsceneJson.toString());
		}

		if (jsonLines > 0 && json.isNotEmpty()) {
			ImGui.inputTextMultiline("###json", json, -1F, 300F, ImGuiInputTextFlags.ReadOnly);
		}

		numberContext = null;
		return update;
	}

	@Override
	public boolean isValid() {
		if (steps.isEmpty()) {
			return false;
		}

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

		return new Cutscene(allowMovement.get(), openPreviousScreen.get(), hidePlayer.get(), list);
	}
}
