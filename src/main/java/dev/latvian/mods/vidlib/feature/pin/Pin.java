package dev.latvian.mods.vidlib.feature.pin;

import imgui.type.ImBoolean;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record Pin(UUID uuid, String name, ImBoolean enabled, ResourceLocation texture, String path) {
}