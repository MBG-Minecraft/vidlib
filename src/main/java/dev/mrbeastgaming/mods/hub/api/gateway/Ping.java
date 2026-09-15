package dev.mrbeastgaming.mods.hub.api.gateway;

import java.time.Instant;

public record Ping(Instant time, String type) {
}
