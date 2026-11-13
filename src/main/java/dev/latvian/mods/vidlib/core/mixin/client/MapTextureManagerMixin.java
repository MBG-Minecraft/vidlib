package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapTextureManager.class)
public class MapTextureManagerMixin {
	@Inject(method = "prepareMapTexture", at = @At("HEAD"), cancellable = true)
	private void vl$getTexture(MapId id, MapItemSavedData data, CallbackInfoReturnable<Identifier> cir) {
		var mc = Minecraft.getInstance();
		var session = ClientSessionData.of(mc);
		var override = session.getMapTextureOverride(id.id());

		if (override != null) {
			cir.setReturnValue(override.getTexture().texturePath());
		}
	}
}
