package dev.latvian.mods.vidlib.feature.screeneffect.chromaticaberration;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import imgui.ImGui;
import net.minecraft.world.phys.Vec2;

public class ChromaticAberrationPanel extends AdminPanel {
	public static final ChromaticAberrationPanel INSTANCE = new ChromaticAberrationPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.ANIMATION, "Chromatic Aberration", INSTANCE);

	public final ImBuilderWrapper<KVector> focusPos = KVectorImBuilder.create();

	private ChromaticAberrationPanel() {
		super("chromatic-aberration", "Chromatic Aberration");
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);

		ImGuiUtils.FLOAT.set(ChromaticAberration.strength);
		ImGui.text("Strength");
		ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), -1F, 1F);
		ChromaticAberration.strength = ImGuiUtils.FLOAT.get();

		ImGuiUtils.BOOLEAN.set(ChromaticAberration.isAngled);
		ImGui.checkbox("Angled###angled", ImGuiUtils.BOOLEAN);
		ChromaticAberration.isAngled = ImGuiUtils.BOOLEAN.get();

		if (ChromaticAberration.isAngled) {
			ImGuiUtils.FLOAT.set(ChromaticAberration.angle);
			ImGui.dragFloat("###angle", ImGuiUtils.FLOAT.getData(), 1F, 0F, 360F);
			ChromaticAberration.angle = ImGuiUtils.FLOAT.get();
		} else {
			ImGui.text("Focus");

			if (ChromaticAberration.screenFocus.x != 0F || ChromaticAberration.screenFocus.y != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-focus")) {
					ChromaticAberration.screenFocus = Vec2.ZERO;
				}
			}

			ImGuiUtils.FLOAT2[0] = ChromaticAberration.screenFocus.x;
			ImGuiUtils.FLOAT2[1] = ChromaticAberration.screenFocus.y;
			ImGui.dragFloat2("###focus", ImGuiUtils.FLOAT2, 0.01F, -1F, 1F);
			ChromaticAberration.screenFocus = new Vec2(ImGuiUtils.FLOAT2[0], ImGuiUtils.FLOAT2[1]);
		}

		ImGui.popItemWidth();
	}
}
