package dev.latvian.mods.vidlib.core.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 26.3 removed {@code net.minecraft.client.renderer.block.model.BlockElement} and its
 * {@code Deserializer.getAngle(JsonObject)} helper. Element geometry is now parsed by
 * {@code net.minecraft.client.resources.model.cuboid.CuboidModelElement.Deserializer}, which reads the
 * rotation angle directly via {@code GsonHelper.getAsFloat(rotationObject, "angle")} - the exact
 * behaviour VidLib's old {@code getAngle} override produced. Kept registered for compatibility;
 * there is no longer a separate {@code getAngle} method to overwrite.
 */
@Mixin(targets = "net.minecraft.client.resources.model.cuboid.CuboidModelElement$Deserializer")
public class BlockElementDeserializerMixin {
}
