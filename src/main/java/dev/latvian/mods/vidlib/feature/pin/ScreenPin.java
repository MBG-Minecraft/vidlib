package dev.latvian.mods.vidlib.feature.pin;

import dev.latvian.mods.vidlib.feature.gallery.GalleryImage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ScreenPin(Entity entity, Pin pin, GalleryImage image, Vec3 pos) {
}