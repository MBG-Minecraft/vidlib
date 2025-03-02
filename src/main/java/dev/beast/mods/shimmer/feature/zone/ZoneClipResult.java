package dev.beast.mods.shimmer.feature.zone;

import net.minecraft.world.phys.Vec3;

public record ZoneClipResult(ZoneInstance instance, ZoneShape shape, double distanceSq, Vec3 pos, Object result) {
}
