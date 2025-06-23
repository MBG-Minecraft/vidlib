package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;

public class SoundDataImBuilder implements ImBuilder<SoundData> {
	private static final Holder<SoundEvent> EMPTY_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY);

	private static final Lazy<Holder<SoundEvent>[]> ALL_SOUNDS = Lazy.of(() -> {
		var list = new ArrayList<Holder<SoundEvent>>();

		for (var sound : BuiltInRegistries.SOUND_EVENT) {
			if (sound != SoundEvents.EMPTY) {
				list.add(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound));
			}
		}

		list.sort((a, b) -> a.getKey().location().compareNamespaced(b.getKey().location()));
		return list.toArray(new Holder[0]);
	});

	private static final SoundSource[] ALL_SOURCES = SoundSource.values();

	public final Holder<SoundEvent>[] sound = new Holder[1];
	public final SoundSource[] source = {SoundSource.PLAYERS};
	public final ImFloat volume = new ImFloat(1F);
	public final ImFloat pitch = new ImFloat(1F);
	public boolean delete = false;

	public SoundDataImBuilder() {
		this.sound[0] = EMPTY_SOUND;
	}

	@Override
	public void set(SoundData value) {
		sound[0] = value.sound();
		source[0] = value.source();
		volume.set(value.volume());
		pitch.set(value.pitch());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		delete = false;
		var update = ImUpdate.NONE;

		if (ImGui.beginCombo("###sound", sound[0] == EMPTY_SOUND ? "Select Sound..." : sound[0].getKey().location().getPath(), ImGuiComboFlags.HeightSmall)) {
			var options = ALL_SOUNDS.get();

			for (int i = 0; i < options.length; i++) {
				var option = options[i];
				boolean isSelected = sound[0] == option;

				if (ImGui.selectable(option.getKey().location() + "###" + i, isSelected)) {
					sound[0] = option;
					update = ImUpdate.FULL;
				}

				if (isValid() && ImGui.isItemClicked(1)) {
					Minecraft.getInstance().playGlobalSound(new SoundData(option, SoundSource.MASTER, volume.get(), pitch.get()));
				}

				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}
			}

			ImGui.endCombo();
		}

		ImGui.alignTextToFramePadding();
		ImGui.text("Source");
		ImGui.sameLine();
		update = update.or(graphics.combo("###source", "", source, ALL_SOURCES));

		ImGui.alignTextToFramePadding();
		ImGui.text("Volume");
		ImGui.sameLine();
		ImGui.sliderFloat("###volume", volume.getData(), 0F, 1F);
		update = update.orItemEdit();

		ImGui.alignTextToFramePadding();
		ImGui.text("Pitch");
		ImGui.sameLine();
		ImGui.sliderFloat("###pitch", pitch.getData(), 0.5F, 2F);
		update = update.orItemEdit();

		return update;
	}

	@Override
	public boolean isValid() {
		return sound[0] != EMPTY_SOUND;
	}

	@Override
	public SoundData build() {
		return new SoundData(sound[0], source[0], volume.get(), pitch.get());
	}
}
