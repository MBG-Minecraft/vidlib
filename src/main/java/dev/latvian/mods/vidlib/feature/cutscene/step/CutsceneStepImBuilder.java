package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.type.ImBoolean;

public abstract class CutsceneStepImBuilder implements ImBuilder<CutsceneStep> {
	public int start = 0;
	public int length = 20;
	public final ImBoolean snap = new ImBoolean(true);
	public CutsceneStepType type;
	public String name = "Step";

	@Override
	public String getDisplayName() {
		return name;
	}
}
