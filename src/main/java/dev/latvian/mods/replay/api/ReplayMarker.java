package dev.latvian.mods.replay.api;

public record ReplayMarker(int time, ReplayMarkerType type, ReplayMarkerData data) {
}
