package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ZoneHit(ResolvedZone resolvedZone, Vec3 pos, double distanceSq, @Nullable Direction direction) {
}
