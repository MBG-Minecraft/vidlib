package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class BoxZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final BoxZoneRenderer INSTANCE = new BoxZoneRenderer();

	@Override
	public void render(ZoneShape shape, ZoneInstance instance, Minecraft mc, RenderLevelStageEvent event) {
		var box = shape.getBoundingBox();
		var cameraPos = event.getCamera().getPosition();
		double minX = box.minX - cameraPos.x;
		double minY = box.minY - cameraPos.y;
		double minZ = box.minZ - cameraPos.z;
		double maxX = box.maxX - cameraPos.x;
		double maxY = box.maxY - cameraPos.y;
		double maxZ = box.maxZ - cameraPos.z;

		LevelRenderer.renderLineBox(event.getPoseStack(), mc.renderBuffers().bufferSource().getBuffer(RenderType.lines()), minX, minY, minZ, maxX, maxY, maxZ, 1F, 1F, 1F, 1F);
	}
}
