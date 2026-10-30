package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

public record PropSubmitContext(
	Minecraft mc,
	SubmitCustomGeometryEvent event,
	PoseStack poseStack,
	SubmitNodeCollector collector
) {
}
