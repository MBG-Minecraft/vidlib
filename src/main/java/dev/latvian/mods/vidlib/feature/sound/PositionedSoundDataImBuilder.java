package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;

import java.util.Optional;

public class PositionedSoundDataImBuilder implements ImBuilder<PositionedSoundData> {
	public final SoundDataImBuilder soundData = new SoundDataImBuilder();
	public final BooleanImBuilder hasPosition = new BooleanImBuilder(false);
	public final ImBuilder<KVector> position = KVectorImBuilder.create();
	public final BooleanImBuilder looping = new BooleanImBuilder(false);
	public final BooleanImBuilder stopImmediately = new BooleanImBuilder(false);
	public boolean delete = false;

	@Override
	public void set(PositionedSoundData value) {
		ImBuilder.super.set(value);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		delete = false;
		var update = soundData.imgui(graphics);
		update = update.or(hasPosition.imguiKey(graphics, "Has Position", "has-position"));

		if (hasPosition.build()) {
			update = update.or(position.imguiKey(graphics, "", "position"));
		}

		update = update.or(looping.imguiKey(graphics, "Looping", "looping"));
		update = update.or(stopImmediately.imguiKey(graphics, "Stop Immediately", "stop-immediately"));
		return update;
	}

	@Override
	public boolean isValid() {
		return soundData.isValid() && hasPosition.isValid() && (!hasPosition.build() || position.isValid()) && looping.isValid() && stopImmediately.isValid();
	}

	@Override
	public PositionedSoundData build() {
		return new PositionedSoundData(
			soundData.build(),
			hasPosition.build() ? Optional.of(position.build()) : Optional.empty(),
			looping.build(),
			stopImmediately.build()
		);
	}
}
