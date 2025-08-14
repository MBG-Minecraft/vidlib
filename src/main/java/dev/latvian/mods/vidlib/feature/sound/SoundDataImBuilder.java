package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.imgui.builder.CompoundImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class SoundDataImBuilder extends CompoundImBuilder<SoundData> {
	private static final List<SoundSource> ALL_SOURCES = List.of(SoundSource.values());

	public static final ImBuilderType<SoundData> TYPE = SoundDataImBuilder::new;
	public static final ImBuilderType<SoundSource> SOURCE_TYPE = () -> new EnumImBuilder<>(ALL_SOURCES, SoundSource.PLAYERS);

	public final SoundEventImBuilder sound = new SoundEventImBuilder();
	public final ImBuilder<SoundSource> source = SOURCE_TYPE.get();
	public final FloatImBuilder volume = new FloatImBuilder(0F, 1F);
	public final FloatImBuilder pitch = new FloatImBuilder(0.5F, 2F, true);

	public SoundDataImBuilder() {
		this.volume.set(1F);
		this.pitch.set(1F);
		add("Sound", sound);
		add("Source", source);
		add("Volume", volume);
		add("Pitch", pitch);
	}

	@Override
	public void set(SoundData value) {
		sound.set(value.sound());
		source.set(value.source());
		volume.set(value.volume());
		pitch.set(value.pitch());
	}

	@Override
	public SoundData build() {
		return new SoundData(sound.build(), source.build(), volume.build(), pitch.build());
	}
}
