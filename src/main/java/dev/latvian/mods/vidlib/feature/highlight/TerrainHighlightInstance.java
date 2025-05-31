package dev.latvian.mods.vidlib.feature.highlight;

public class TerrainHighlightInstance {
	public final TerrainHighlight highlight;
	public int tick;

	public TerrainHighlightInstance(TerrainHighlight highlight) {
		this.highlight = highlight;
		this.tick = 0;
	}
}
