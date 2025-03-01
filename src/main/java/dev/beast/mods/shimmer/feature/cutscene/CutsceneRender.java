package dev.beast.mods.shimmer.feature.cutscene;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@FunctionalInterface
public interface CutsceneRender {
	void render(Minecraft mc, RenderLevelStageEvent ctx, float delta, float progress, Vec3 target);
}
