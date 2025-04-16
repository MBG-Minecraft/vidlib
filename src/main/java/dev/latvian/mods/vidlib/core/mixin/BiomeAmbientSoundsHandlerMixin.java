package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BiomeAmbientSoundsHandler.class)
public class BiomeAmbientSoundsHandlerMixin {
	/**
	 * @author Lat
	 * @reason Remove all mood sounds
	 */
	@Overwrite
	public void tick() {
	}
}
