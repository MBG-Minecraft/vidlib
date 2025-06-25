package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class SoundDataImBuilder implements ImBuilder<SoundData> {
	private static final List<SoundSource> ALL_SOURCES = List.of(SoundSource.values());

	public final SoundEventImBuilder sound = new SoundEventImBuilder();
	public final SoundSource[] source = {SoundSource.PLAYERS};
	public final ImFloat volume = new ImFloat(1F);
	public final ImFloat pitch = new ImFloat(1F);
	public boolean delete = false;

	@Override
	public void set(SoundData value) {
		sound.set(value.sound());
		source[0] = value.source();
		volume.set(value.volume());
		pitch.set(value.pitch());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		delete = false;
		var update = ImUpdate.NONE;

		ImGui.alignTextToFramePadding();
		ImGui.text("Sound");
		ImGui.sameLine();
		update = update.or(sound.imgui(graphics));

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
		return sound.isValid();
	}

	@Override
	public SoundData build() {
		return new SoundData(sound.build(), source[0], volume.get(), pitch.get());
	}
}
