package dev.latvian.mods.vidlib.util.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

import javax.annotation.Nullable;

public class VLSectionToNodeMap extends SectionOcclusionGraph.SectionToNodeMap {
	private final Long2ObjectMap<SectionOcclusionGraph.Node> map;

	public VLSectionToNodeMap() {
		super(0);
		this.map = new Long2ObjectOpenHashMap<>();
	}

	@Override
	public void put(SectionRenderDispatcher.RenderSection section, SectionOcclusionGraph.Node node) {
		if (node == null) {
			map.remove(section.getSectionNode());
		} else {
			map.put(section.getSectionNode(), node);
		}
	}

	@Override
	@Nullable
	public SectionOcclusionGraph.Node get(SectionRenderDispatcher.RenderSection section) {
		return map.get(section.getSectionNode());
	}
}
