package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.util.FrameInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface CutsceneRender {
	void render(Minecraft mc, FrameInfo frame, float delta, float progress, Vec3 target);
}
