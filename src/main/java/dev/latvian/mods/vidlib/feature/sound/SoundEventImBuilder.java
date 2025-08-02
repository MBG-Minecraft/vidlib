package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderSupplier;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SoundEventImBuilder implements ImBuilder<Holder<SoundEvent>> {
	public static final Holder<SoundEvent> EMPTY_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY);
	public static final ImBuilderSupplier<Holder<SoundEvent>> SUPPLIER = SoundEventImBuilder::new;

	public static final Lazy<List<Holder<SoundEvent>>> ALL_SOUNDS = Lazy.of(() -> {
		var list = new ArrayList<Holder<SoundEvent>>();

		for (var sound : BuiltInRegistries.SOUND_EVENT) {
			if (sound != SoundEvents.EMPTY) {
				list.add(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound));
			}
		}

		list.sort((a, b) -> a.getKey().location().compareNamespaced(b.getKey().location()));
		return List.copyOf(list);
	});

	public static final ImFloat PREVIEW_VOLUME = new ImFloat(1F);
	public static final ImFloat PREVIEW_PITCH = new ImFloat(1F);
	public static final ImBoolean PREVIEW_OPEN = new ImBoolean();
	public static final ImString PREVIEW_SEARCH = ImGuiUtils.resizableString();

	public Holder<SoundEvent>[] sound = new Holder[1];

	public SoundEventImBuilder() {
		sound[0] = EMPTY_SOUND;
	}

	@Override
	public void set(Holder<SoundEvent> value) {
		if (value == null || value.value() == SoundEvents.EMPTY) {
			sound[0] = EMPTY_SOUND;
		} else {
			sound[0] = value;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (ImGui.button((sound[0] == EMPTY_SOUND ? "Select Sound..." : sound[0].getKey().location().getPath()) + "###sound")) {
			ImGui.openPopup("###sound-modal");
		}

		update = update.or(soundModal(graphics, sound));
		return update;
	}

	public static ImUpdate soundModal(ImGraphics graphics, @Nullable Holder<SoundEvent>[] sound) {
		var update = ImUpdate.NONE;
		var viewport = ImGui.getMainViewport();
		ImGui.setNextWindowSizeConstraints(700F, 500F, viewport.getWorkSizeX(), viewport.getWorkSizeY());

		PREVIEW_OPEN.set(true);
		if (ImGui.beginPopupModal("Sounds###sound-modal", PREVIEW_OPEN, ImGuiWindowFlags.NoSavedSettings)) {
			ImGui.columns(3);

			ImGui.setNextItemWidth(-1F);
			ImGui.inputTextWithHint("###search", "Search...", PREVIEW_SEARCH);

			ImGui.nextColumn();

			if (ImGui.button(ImIcons.STOP + "###stop-all-sounds")) {
				graphics.mc.getSoundManager().stop();
			}

			if (ImGui.isItemHovered()) {
				ImGui.setTooltip("Stop All Sounds");
			}

			ImGui.sameLine();

			ImGui.setNextItemWidth(-1F);
			ImGui.sliderFloat("###volume", PREVIEW_VOLUME.getData(), 0F, 1F);

			if (ImGui.isItemHovered()) {
				ImGui.setTooltip("Volume");
			}

			ImGui.nextColumn();

			ImGui.setNextItemWidth(-1F);
			ImGui.sliderFloat("###pitch", PREVIEW_PITCH.getData(), 0.5F, 2F);

			if (ImGui.isItemHovered()) {
				ImGui.setTooltip("Pitch");
			}

			ImGui.columns();

			if (ImGui.beginListBox("###sound-list", -1F, -1F)) {
				var options = ALL_SOUNDS.get();

				for (int i = 0; i < options.size(); i++) {
					var option = options.get(i);
					boolean isSelected = sound != null && sound[0] == option;

					if (!PREVIEW_SEARCH.get().isEmpty()) {
						if (!option.getKey().location().getPath().contains(PREVIEW_SEARCH.get())) {
							continue;
						}
					}

					if (ImGui.smallButton(ImIcons.PLAY + "###preview" + i)) {
						graphics.mc.playGlobalSound(new SoundData(option, SoundSource.MASTER, PREVIEW_VOLUME.get(), PREVIEW_PITCH.get()));
					}

					ImGui.sameLine();

					if (ImGui.selectable(option.getKey().location() + "###" + i, isSelected)) {
						if (sound != null) {
							sound[0] = option;
							update = ImUpdate.FULL;
							PREVIEW_OPEN.set(false);
							ImGui.closeCurrentPopup();
						}
					}

					if (isSelected) {
						ImGui.setItemDefaultFocus();
					}
				}

				ImGui.endListBox();
			}

			ImGui.endPopup();
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return sound[0] != EMPTY_SOUND;
	}

	@Override
	public Holder<SoundEvent> build() {
		return sound[0];
	}
}
