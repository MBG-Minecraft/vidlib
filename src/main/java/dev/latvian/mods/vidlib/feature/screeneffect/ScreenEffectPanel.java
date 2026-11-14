package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ScreenEffectPanel extends Panel {
	public static final ScreenEffectPanel INSTANCE = new ScreenEffectPanel();

	public final int[] duration = {100};

	private ScreenEffectPanel() {
		super("screen-effects", "Screen Effects");
	}

	private void add(ImGraphics graphics, DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type, ScreenEffectClientInfo<?> clientInfo) {
		if (clientInfo.stateFactory == null) {
			ImGui.beginDisabled();
		}

		if (ImGui.button(clientInfo.icon + "###add-" + type.key())) {
			if (clientInfo.stateFactory != null) {
				/*
				var inst = effect.get();
				inst.duration = duration[0];
				inst.name = tooltip;
				var ctx = LevelSessionData.of(graphics.mc.level).createGlobalContext().fork(inst.variables);
				ctx.progress = 0D;
				inst.extract(ctx);
				graphics.session.screenEffects.add(inst);
				 */
			}
		}

		if (clientInfo.stateFactory == null) {
			ImGui.endDisabled();
		}

		graphics.hoveredTooltip(clientInfo.displayName);
	}

	@Override
	public void content(ImGraphics graphics) {
		if (graphics.session == null) {
			return;
		}

		ImGui.pushItemWidth(-1F);

		ImGui.alignTextToFramePadding();
		ImGui.text("Add");
		ImGui.sameLine();
		ImGui.sliderInt("###duration", duration, 0, 1200, KMath.NumberFormat.VERY_SHORT.format(duration[0] / 20F) + " s");
		graphics.hoveredTooltip("Duration");

		int count = 0;

		for (var type : ScreenEffect.REGISTRY.typeList()) {
			var clientInfo = type instanceof DynamicType<?, ?> dynamicType ? ScreenEffectClientInfo.get(dynamicType) : null;

			if (clientInfo != null) {
				if (count % 5 != 0) {
					ImGui.sameLine();
				}

				add(graphics, (DynamicType<RegistryFriendlyByteBuf, ScreenEffect>) type, clientInfo);
				count++;
			}
		}

		ImGui.separator();

		var effects = graphics.session.screenEffects;
		var delta = graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		for (int i = 0; i < effects.size(); i++) {
			if (i > 0) {
				ImGui.separator();
			}

			var effect = effects.get(i);

			ImGui.pushID(i);
			ImGui.alignTextToFramePadding();
			ImGui.text(effect.name);
			ImGui.sameLine();

			if (graphics.button(ImIcons.TRASHCAN + "###remove", ImColorVariant.RED)) {
				effects.remove(i);
				i--;
			}

			graphics.hoveredTooltip("Remove");
			ImGui.sameLine();

			if (graphics.button((effect.paused ? ImIcons.PLAY : ImIcons.PAUSE) + "###pause", ImColorVariant.GRAY)) {
				effect.paused = !effect.paused;
			}

			graphics.hoveredTooltip(effect.paused ? "Resume" : "Pause");
			ImGui.sameLine();

			if (effect.paused) {
				ImGuiUtils.FLOAT.set(effect.tick / (float) effect.duration);
				if (ImGui.sliderFloat("###progress", ImGuiUtils.FLOAT.getData(), 0F, 1F)) {
					effect.tick = (int) (ImGuiUtils.FLOAT.get() * effect.duration);
				}
			} else {
				ImGui.progressBar((effect.tick + delta) / (float) effect.duration);

				if (ImGui.isItemClicked()) {
					effect.tick = 0;
				}
			}

			effect.imgui(graphics);

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}
}
