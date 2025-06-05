package dev.latvian.mods.vidlib.util.client;

import dev.latvian.mods.vidlib.VidLibConfig;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class VLViewArea extends ViewArea {
	private final SectionRenderDispatcher dispatcher;
	private final int minSectionY;
	private final int maxSectionY;
	private final Long2ObjectMap<SectionRenderDispatcher.RenderSection>[] maps;

	public VLViewArea(SectionRenderDispatcher dispatcher, Level level, int viewDistance, LevelRenderer levelRenderer) {
		super(dispatcher, level, 0, levelRenderer);
		this.dispatcher = dispatcher;
		this.minSectionY = level.getMinSectionY();
		this.maxSectionY = level.getMaxSectionY();
		this.maps = new Long2ObjectOpenHashMap[maxSectionY - minSectionY + 1];

		for (int y = 0; y < maps.length; y++) {
			maps[y] = new Long2ObjectOpenHashMap<>();
		}

		this.cameraSectionPos = SectionPos.of(99999, 0, 99999);
	}

	@Override
	public int getViewDistance() {
		return VidLibConfig.clientRenderDistance;
	}

	@Override
	public void releaseAllBuffers() {
		for (var map : maps) {
			for (var s : map.values()) {
				s.reset();
			}

			map.clear();
		}
	}

	@Override
	public void repositionCamera(SectionPos newSectionPos) {
		int viewDistance = getViewDistance();
		int hViewDistance = Math.min(32, viewDistance);

		int minY = Math.max(newSectionPos.getY() - hViewDistance, minSectionY);
		int maxY = Math.min(newSectionPos.getY() + hViewDistance, maxSectionY);

		for (int sy = minY; sy <= maxY; sy++) {
			var map = maps[sy - minSectionY];

			for (int x = -viewDistance; x <= viewDistance; x++) {
				for (int z = -viewDistance; z <= viewDistance; z++) {
					if (x * x + z * z > viewDistance * viewDistance) {
						continue;
					}

					var sectionPos = SectionPos.asLong(newSectionPos.x() + x, sy, newSectionPos.z() + z);
					var section = getRenderSection(sectionPos);

					if (section == null) {
						section = dispatcher.new RenderSection(-1, sectionPos);
						map.put(sectionPos, section);
						section.setSectionNode(sectionPos);
					}
				}
			}
		}

		this.cameraSectionPos = newSectionPos;
		this.levelRenderer.getSectionOcclusionGraph().invalidate();
	}

	@Override
	public void setDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
		var s = getRenderSection(SectionPos.asLong(sectionX, sectionY, sectionZ));

		if (s != null) {
			s.setDirty(reRenderOnMainThread);
		}
	}

	@Override
	@Nullable
	protected SectionRenderDispatcher.RenderSection getRenderSection(long sectionPos) {
		int si = SectionPos.y(sectionPos) - minSectionY;
		return si < 0 || si >= maps.length ? null : maps[si].get(sectionPos);
	}
}
