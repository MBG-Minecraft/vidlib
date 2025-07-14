package dev.latvian.mods.vidlib.feature.stage;

public class Scene {
	public final String id;
	public Stage stage;
	public int index;

	public Scene(String id) {
		this.id = id;
		this.index = -1;
	}
}
