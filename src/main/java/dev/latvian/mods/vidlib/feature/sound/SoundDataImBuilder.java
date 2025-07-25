package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class SoundDataImBuilder implements ImBuilder<SoundData> {
	private static final List<SoundSource> ALL_SOURCES = List.of(SoundSource.values());

	public final SoundEventImBuilder sound = new SoundEventImBuilder();
	public final EnumImBuilder<SoundSource> source = new EnumImBuilder<>(SoundSource[]::new, ALL_SOURCES, SoundSource.PLAYERS);
	public final FloatImBuilder volume = new FloatImBuilder(1F, 0F, 1F);
	public final FloatImBuilder pitch = new FloatImBuilder(1F, 0.5F, 2F).logarithmic();
	public boolean delete = false;

	@Override
	public void set(SoundData value) {
		sound.set(value.sound());
		source.set(value.source());
		volume.set(value.volume());
		pitch.set(value.pitch());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		delete = false;
		var update = ImUpdate.NONE;
		update = update.or(sound.imguiKey(graphics, "Sound", "sound"));
		update = update.or(source.imguiKey(graphics, "Source", "source"));
		update = update.or(volume.imguiKey(graphics, "Volume", "volume"));
		update = update.or(pitch.imguiKey(graphics, "Pitch", "pitch"));
		return update;
	}

	@Override
	public boolean isValid() {
		return sound.isValid() && source.isValid() && volume.isValid() && pitch.isValid();
	}

	@Override
	public SoundData build() {
		return new SoundData(sound.build(), source.build(), volume.build(), pitch.build());
	}
}
