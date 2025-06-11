package dev.latvian.mods.vidlib.feature.prop;

public record PropContext<P extends Prop>(Props<?> props, PropType<P> type, PropSpawnType spawnType, long createdTime) {
}
