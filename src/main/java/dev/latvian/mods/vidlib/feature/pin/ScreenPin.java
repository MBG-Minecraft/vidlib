package dev.latvian.mods.vidlib.feature.pin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ScreenPin(Entity entity, Pin pin, Vec3 pos) {
}