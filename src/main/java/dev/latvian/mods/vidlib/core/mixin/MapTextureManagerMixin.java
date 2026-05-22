package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapTextureManager.class)
public class MapTextureManagerMixin {
	@Inject(method = "prepareMapTexture", at = @At("HEAD"), cancellable = true)
	private void vl$getTexture(MapId id, MapItemSavedData data, CallbackInfoReturnable<ResourceLocation> cir) {
		var player = Minecraft.getInstance().player;

		if (player != null) {
			var override = player.vl$sessionData().getMapTextureOverride(id.id());

			if (override != null) {
				cir.setReturnValue(override.getTexture());
			}
		}
	}
}
