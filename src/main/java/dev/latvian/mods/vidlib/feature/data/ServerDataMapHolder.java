package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ServerDataMapHolder extends DataMapHolder {
	default ResourceLocation getSkybox() {
		return getOptional(InternalServerData.SKYBOX);
	}

	default void setSkybox(ResourceLocation skybox) {
		set(InternalServerData.SKYBOX, skybox);
	}

	default Anchor getAnchor() {
		return get(InternalServerData.ANCHOR);
	}

	default void setAnchor(Anchor anchor) {
		set(InternalServerData.ANCHOR, anchor);
	}

	default NameDrawType getNameDrawType() {
		return getOptional(InternalServerData.NAME_DRAW_TYPE);
	}

	default void setNameDrawType(NameDrawType type) {
		set(InternalServerData.NAME_DRAW_TYPE, type);
	}

	default void setNameDrawDistance(double min, double mid, double max) {
		set(InternalServerData.NAME_DRAW_MIN_DIST, min);
		set(InternalServerData.NAME_DRAW_MID_DIST, mid);
		set(InternalServerData.NAME_DRAW_MAX_DIST, max);
	}

	default List<Waypoint> getWaypoints() {
		return get(InternalServerData.WAYPOINTS);
	}

	default void setWaypoints(List<Waypoint> waypoints) {
		set(InternalServerData.WAYPOINTS, List.copyOf(waypoints));
	}

	default void addWaypoints(List<Waypoint> waypoints) {
		var list = new ArrayList<>(getWaypoints());
		list.addAll(waypoints);
		setWaypoints(List.copyOf(list));
	}

	default void removeWaypoints(Collection<String> ids) {
		var list = new ArrayList<Waypoint>();

		for (var waypoint : getWaypoints()) {
			if (!ids.contains(waypoint.id())) {
				list.add(waypoint);
			}
		}

		setWaypoints(List.copyOf(list));
	}
}
