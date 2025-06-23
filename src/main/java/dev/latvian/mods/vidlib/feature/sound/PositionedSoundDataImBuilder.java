package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVectorImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.Optional;

public class PositionedSoundDataImBuilder implements ImBuilder<PositionedSoundData> {
	public final SoundDataImBuilder soundData = new SoundDataImBuilder();
	public final ImBoolean hasPosition = new ImBoolean(true);
	public final ImBuilder<WorldVector> position = WorldVectorImBuilder.create();
	public final ImBoolean looping = new ImBoolean(false);
	public final ImBoolean stopImmediately = new ImBoolean(false);

	@Override
	public void set(PositionedSoundData value) {
		ImBuilder.super.set(value);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = soundData.imgui(graphics);
		update = update.or(ImGui.checkbox("Has Position###has-position", hasPosition));

		if (hasPosition.get()) {
			ImGui.pushID("###position");
			update = update.or(position.imgui(graphics));
			ImGui.popID();
		}

		update = update.or(ImGui.checkbox("Looping###looping", looping));
		update = update.or(ImGui.checkbox("Stop Immediately###stop-immediately", stopImmediately));
		return update;
	}

	@Override
	public boolean isValid() {
		return soundData.isValid() && position.isValid();
	}

	@Override
	public PositionedSoundData build() {
		return new PositionedSoundData(
			soundData.build(),
			hasPosition.get() ? Optional.of(position.build()) : Optional.empty(),
			looping.get(),
			stopImmediately.get()
		);
	}
}
