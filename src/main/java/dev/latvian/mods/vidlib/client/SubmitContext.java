package dev.latvian.mods.vidlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;

public record SubmitContext(
	Minecraft mc,
	@Nullable SubmitCustomGeometryEvent event,
	PoseStack poseStack,
	SubmitNodeCollector collector
) {
}
