package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameDrawType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ServerDataMapHolder extends DataMapHolder {
	@Nullable
	default Ref<Atmosphere> getAtmosphere() {
		return get(InternalServerData.ATMOSPHERE);
	}

	default void setAtmosphere(@Nullable Ref<Atmosphere> atmosphere) {
		set(InternalServerData.ATMOSPHERE, atmosphere);
	}

	default Anchor getAnchor() {
		return get(InternalServerData.ANCHOR);
	}

	default void setAnchor(Anchor anchor) {
		set(InternalServerData.ANCHOR, anchor);
	}

	default NameDrawType getNameDrawType() {
		return get(InternalServerData.NAME_DRAW_TYPE);
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
		setWaypoints(list);
	}

	default void removeWaypoints(Collection<String> ids) {
		var list = new ArrayList<Waypoint>();

		for (var waypoint : getWaypoints()) {
			if (!ids.contains(waypoint.id())) {
				list.add(waypoint);
			}
		}

		setWaypoints(list);
	}
}
