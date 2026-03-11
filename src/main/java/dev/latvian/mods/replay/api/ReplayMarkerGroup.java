package dev.latvian.mods.replay.api;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.color.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ReplayMarkerGroup {
	public static final Map<String, ReplayMarkerGroup> GROUPS = new HashMap<>();

	public static ReplayMarkerGroup get(String id) {
		return GROUPS.computeIfAbsent(id, ReplayMarkerGroup::new);
	}

	public static ReplayMarkerGroup make(String id, Consumer<ReplayMarkerGroup> callback) {
		var group = get(id);
		callback.accept(group);
		return group;
	}

	public static Codec<ReplayMarkerGroup> CODEC = Codec.STRING.xmap(ReplayMarkerGroup::get, g -> g.id);

	public static final ReplayMarkerGroup DEFAULT = make("default", group -> {
		group.displayName = "Default";
		group.defaultColor = Color.GREEN;
	});

	public final String id;
	public String displayName;
	public Color defaultColor;
	public boolean visible;

	private ReplayMarkerGroup(String id) {
		this.id = id;
		this.displayName = "";
		this.defaultColor = Color.WHITE;
		this.visible = true;
	}

	public String getDisplayName() {
		return displayName.isEmpty() ? id : displayName;
	}
}
