package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;

public class SoundDataImBuilder implements ImBuilder<SoundData> {
	private static final Lazy<Holder<SoundEvent>[]> ALL_SOUNDS = Lazy.of(() -> {
		var list = new ArrayList<Holder<SoundEvent>>();

		for (var sound : BuiltInRegistries.SOUND_EVENT) {
			list.add(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound));
		}

		return list.toArray(new Holder[0]);
	});

	private static final SoundSource[] ALL_SOURCES = SoundSource.values();

	public final Holder<SoundEvent>[] sound = new Holder[1];
	public final SoundSource[] source = {SoundSource.PLAYERS};
	public final ImFloat volume = new ImFloat(1F);
	public final ImFloat pitch = new ImFloat(1F);

	@Override
	public void set(SoundData value) {
		sound[0] = value.sound();
		source[0] = value.source();
		volume.set(value.volume());
		pitch.set(value.pitch());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		update = graphics.combo("Sound###sound", "Select sound...", sound, ALL_SOUNDS.get(), h -> h.getKey().location().toString(), 0);
		update = update.or(graphics.combo("Source###source", "", source, ALL_SOURCES));
		ImGui.sliderFloat("Volume###volume", volume.getData(), 0F, 1F);
		update = update.or(ImUpdate.itemEdit());
		ImGui.sliderFloat("Pitch###pitch", pitch.getData(), 0.5F, 2F);
		update = update.or(ImUpdate.itemEdit());
		return update;
	}

	@Override
	public boolean isValid() {
		return sound[0] != null;
	}

	@Override
	public SoundData build() {
		return new SoundData(sound[0], source[0], volume.get(), pitch.get());
	}
}
