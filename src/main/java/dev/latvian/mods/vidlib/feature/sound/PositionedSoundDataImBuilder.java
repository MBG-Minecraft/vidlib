package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.CompoundImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import imgui.type.ImBoolean;

import java.util.Optional;

public class PositionedSoundDataImBuilder extends CompoundImBuilder<PositionedSoundData> {
	public final SoundDataImBuilder soundData = new SoundDataImBuilder();
	public final ImBoolean hasPosition = new ImBoolean();
	public final ImBuilder<KVector> position = KVectorImBuilder.create();
	public final BooleanImBuilder looping = new BooleanImBuilder();
	public final BooleanImBuilder stopImmediately = new BooleanImBuilder();

	public PositionedSoundDataImBuilder() {
		add("Sound Data", soundData);
		add("Position", position, hasPosition);
		add("Looping", looping);
		add("Stop Immediately", stopImmediately);
	}

	@Override
	public void set(PositionedSoundData value) {
		soundData.set(value.data());

		if (value.position().isPresent()) {
			hasPosition.set(true);
			position.set(value.position().get());
		} else {
			hasPosition.set(false);
		}

		looping.set(value.looping());
		stopImmediately.set(value.stopImmediately());
	}

	@Override
	public PositionedSoundData build() {
		return new PositionedSoundData(
			soundData.build(),
			hasPosition.get() ? Optional.of(position.build()) : Optional.empty(),
			looping.build(),
			stopImmediately.build()
		);
	}
}
