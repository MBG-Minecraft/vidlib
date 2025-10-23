package dev.latvian.mods.vidlib.feature.environment;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.DoubleImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vec3ImBuilder;
import dev.latvian.mods.vidlib.feature.misc.FlashbackIntegration;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public class WorldBorderOverrideImBuilder implements ImBuilder<WorldBorderOverride> {
	public final Vec3ImBuilder position;
	public final DoubleImBuilder size;
	public final boolean hasTime;
	public long time;

	public WorldBorderOverrideImBuilder(boolean hasTime) {
		this.position = new Vec3ImBuilder();
		this.size = new DoubleImBuilder(0D, 1024D * 10);
		this.size.set(100D);
		this.hasTime = hasTime;
		this.time = 0L;
	}

	@Override
	public void set(@Nullable WorldBorderOverride value) {
		if (value != null) {
			position.set(value.pos());
			size.set(value.size());
			time = value.time();
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		update = update.or(position.imguiKey(graphics, "Position", "position"));
		update = update.or(size.imguiKey(graphics, "Size", "size"));

		if (graphics.isReplay && hasTime) {
			long t = time - FlashbackIntegration.getStartTick();

			if (ImGui.smallButton(time == 0L ? "Set Time###set-time" : "Set Time (%.01f s / %,d)###set-time".formatted(t / 20D, t))) {
				time = graphics.mc.level.getGameTime();
			}
		} else {
			time = 0L;
		}

		return update;
	}

	@Override
	public WorldBorderOverride build() {
		return new WorldBorderOverride(time, position.build(), size.build());
	}
}
