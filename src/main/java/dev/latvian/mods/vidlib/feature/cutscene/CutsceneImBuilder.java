package dev.latvian.mods.vidlib.feature.cutscene;

import com.google.gson.JsonElement;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStepImBuilder;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStepType;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.util.JsonUtils;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class CutsceneImBuilder implements ImBuilder<Cutscene> {
	public final KNumberVariables variables;
	public final List<CutsceneStepImBuilder> steps = new ArrayList<>();
	public final ImBoolean allowMovement = new ImBoolean(false);
	public final ImBoolean openPreviousScreen = new ImBoolean(false);
	public final ImBoolean hidePlayer = new ImBoolean(false);
	public Cutscene cutscene = null;
	public JsonElement cutsceneJson = null;
	public final TextEditor jsonEditor;

	public CutsceneImBuilder() {
		this.variables = new KNumberVariables();

		this.jsonEditor = new TextEditor();
		this.jsonEditor.setReadOnly(true);
		this.jsonEditor.setTabSize(2);
		this.jsonEditor.setShowWhitespaces(false);
		this.jsonEditor.setText("");

		// var lang = TextEditorLanguageDefinition.c();
		// this.jsonEditor.setLanguageDefinition(lang);
	}

	private static String t2s(int ticks) {
		float f = ticks / 20F;
		return (f == (int) f ? String.valueOf((int) f) : String.valueOf(f)) + "s";
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		String previewTooltip;

		if (cutscene != null) {
			int len = 0;

			for (var steps : cutscene.steps) {
				len = Math.max(len, steps.start + steps.length);
			}

			previewTooltip = "Preview\nTotal length: " + t2s(len);
		} else {
			previewTooltip = "Preview (Invalid)";
		}

		if (cutscene == null) {
			ImGui.beginDisabled();
		}

		if (graphics.iconButton(ImIcons.PLAY, "###preview-cutscene", previewTooltip, null)) {
			if (cutscene != null) {
				try {
					if (graphics.isAdmin) {
						graphics.mc.c2s(new PreviewCutscenePayload(cutscene, variables));
					} else {
						graphics.mc.playCutscene(cutscene, variables);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		if (cutscene == null) {
			ImGui.endDisabled();
		}

		ImGui.sameLine();
		update = update.or(graphics.toggleButton(ImIcons.FREEZE, "###allow-movement", "Allow Movement", allowMovement));
		ImGui.sameLine();
		update = update.or(graphics.toggleButton(ImIcons.UNDO, "###open-previous-screen", "Open Previous Screen", openPreviousScreen));
		ImGui.sameLine();
		update = update.or(graphics.toggleButton(ImIcons.INVISIBLE, "###hide-player", "Hide Player", hidePlayer));
		ImGui.sameLine();

		if (graphics.iconButton(ImIcons.ADD, "###add-step", "Add Step", ImColorVariant.GREEN)) {
			ImGui.openPopup("###add-step-popup");
		}

		ImGui.sameLine();

		if (graphics.iconButton(ImIcons.CODE, "###get-json", "Get JSON", null)) {
			ImGui.openPopup("###get-json-popup");
		}

		ImGui.separator();

		if (ImGui.beginPopup("###add-step-popup", ImGuiWindowFlags.NoSavedSettings)) {
			graphics.pushStack();
			graphics.setButtonTextAlign(0F, 0.5F);

			float width = 0F;

			for (var type : CutsceneStepType.VALUES) {
				width = Math.max(width, ImGui.calcTextSize(type.displayName).x);
			}

			for (var type : CutsceneStepType.VALUES) {
				if (ImGui.button(type.icon + " " + type.displayName + "###" + type.getSerializedName(), width + 38F, 0F)) {
					var step = type.factory.get();
					var builder = step.createBuilder();
					builder.set(step);
					builder.start = step.start;
					builder.length = step.length;
					builder.type = type;
					builder.name = type.displayName;
					steps.add(builder);
					update = ImUpdate.FULL;
					ImGui.closeCurrentPopup();
				}
			}

			graphics.popStack();
			ImGui.endPopup();
		}

		if (ImGui.beginPopup("Cutscene JSON###get-json-popup", ImGuiWindowFlags.NoSavedSettings)) {
			ImGui.alignTextToFramePadding();
			ImGui.text("JSON");
			ImGui.sameLine();

			if (cutsceneJson == null) {
				ImGui.beginDisabled();
			}

			if (ImGui.button(ImIcons.COPY + "###copy-json")) {
				ImGui.setClipboardText(cutsceneJson.toString());
			}

			if (cutsceneJson == null) {
				ImGui.endDisabled();
			}

			ImGuiUtils.hoveredTooltip("Copy to Clipboard");

			if (ImGui.beginChild("###json", 400F, 400F, false)) {
				jsonEditor.render("JSON");
			}

			ImGui.endChild();

			ImGui.endPopup();
		}

		ImGui.pushID("###steps");

		for (int i = 0; i < steps.size(); i++) {
			var b = steps.get(i);
			ImGui.pushID(i);

			ImGuiUtils.BOOLEAN.set(true);

			if (graphics.collapsingHeader(b.getDisplayName(), ImGuiUtils.BOOLEAN, ImGuiTreeNodeFlags.DefaultOpen)) {
				if (!ImGuiUtils.BOOLEAN.get()) {
					steps.remove(i);
					i--;
					update = ImUpdate.FULL;
				} else {
					ImGui.columns(2);

					ImGui.text("Start");
					ImGuiUtils.FLOAT.set(b.start / 20F);
					ImGui.setNextItemWidth(-1F);
					if (ImGui.dragFloat("###start", ImGuiUtils.FLOAT.getData(), 0.5F, 0F, 60F, "%.1f", 0)) {
						b.start = Mth.floor(ImGuiUtils.FLOAT.get() * 20F);
					}

					update = update.orItemEdit();

					ImGui.nextColumn();

					ImGui.text("Length");
					ImGuiUtils.FLOAT.set(b.length / 20F);
					ImGui.setNextItemWidth(-1F);
					if (ImGui.dragFloat("###length", ImGuiUtils.FLOAT.getData(), 0.5F, 1F, 60F, "%.1f", 0)) {
						b.length = Mth.floor(ImGuiUtils.FLOAT.get() * 20F);
					}

					update = update.orItemEdit();

					ImGui.nextColumn();
					ImGui.columns();

					if (b.type.hasSnap && ImGui.checkbox("Snap###snap", b.snap)) {
						update = ImUpdate.FULL;
					}

					if (b.start < 0) {
						b.name = b.type.icon + " " + b.type.displayName + " (Invalid Start)";
					} else if (b.length <= 0) {
						b.name = b.type.icon + " " + b.type.displayName + " (Invalid Length)";
					} else {
						b.name = b.type.icon + " " + b.type.displayName + " @ " + t2s(b.start);

						if (b.length >= 1) {
							b.name += " for " + t2s(b.length);
						}
					}

					ImGui.pushID("###data");
					update = update.or(b.imgui(graphics));
					ImGui.popID();
				}
			}

			ImGui.popID();
		}

		ImGui.popID();

		ImGui.separator();

		if (update.isFull()) {
			cutscene = null;
			cutsceneJson = null;
			jsonEditor.setText("");

			if (isValid()) {
				try {
					var c = build();
					cutsceneJson = JsonUtils.sort(Cutscene.DIRECT_CODEC.encodeStart(graphics.mc.level.jsonOps(), c).getOrThrow());
					jsonEditor.setText(JsonUtils.prettyString(cutsceneJson));
					cutscene = c;
				} catch (Exception ex) {
					graphics.stackTrace(ex);
				}
			}
		}

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
		var c = new Cutscene();
		c.allowMovement = allowMovement.get();
		c.openPreviousScreen = openPreviousScreen.get();
		c.hidePlayer = hidePlayer.get();

		for (var step : steps) {
			var s = step.build();
			s.start = step.start;
			s.length = step.length;
			s.snap = step.snap.get();
			c.steps.add(s);
		}

		return c;
	}
}
