package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class BoxZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final BoxZoneRenderer INSTANCE = new BoxZoneRenderer();

	@Override
	public void render(ZoneShape shape, Minecraft mc, RenderLevelStageEvent event, float delta, Color color, Color outlineColor) {
		var box = shape.getBoundingBox();
		var cameraPos = event.getCamera().getPosition();
		float minX = (float) (box.minX - cameraPos.x);
		float minY = (float) (box.minY - cameraPos.y);
		float minZ = (float) (box.minZ - cameraPos.z);
		float maxX = (float) (box.maxX - cameraPos.x);
		float maxY = (float) (box.maxY - cameraPos.y);
		float maxZ = (float) (box.maxZ - cameraPos.z);

		BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, event.getPoseStack(), mc.renderBuffers().bufferSource().getBuffer(ShimmerRenderTypes.DEBUG_LINES), outlineColor);
		BoxRenderer.renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, event.getPoseStack(), mc.renderBuffers().bufferSource().getBuffer(ShimmerRenderTypes.DEBUG_QUADS), color);
	}
}
