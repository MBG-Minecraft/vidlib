package dev.latvian.mods.vidlib.core.mixin.mod;

import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.gui.volume.CategoryVolumeEntry;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumeEntry;
import de.maxhenkel.voicechat.gui.volume.VolumeEntry;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AdjustVolumeList.class)
public abstract class AdjustVolumeListMixin {
	@Shadow
	@Final
	protected List<VolumeEntry> entries;

	@Shadow
	protected AdjustVolumesScreen screen;

	@Shadow
	public abstract void updateFilter();

	@Inject(method = "updateEntryList", at = @At("HEAD"), cancellable = true)
	private void vl$updateEntryList(CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.hideVoiceChatPlayerList()) {
			entries.clear();

			for (var category : ClientManager.getCategoryManager().getCategories()) {
				entries.add(new CategoryVolumeEntry(category, screen));
			}

			if (screen.getMinecraft().level != null && screen.getMinecraft().player != null) {
				for (var state : ClientManager.getPlayerStateManager().getPlayerStates(false)) {
					var player = screen.getMinecraft().level.getPlayerByUUID(state.getUuid());

					if (player != null && player.distanceToSqr(screen.getMinecraft().player) <= 30D * 30D) {
						entries.add(new PlayerVolumeEntry(state, screen));
					}
				}
			}

			updateFilter();
			ci.cancel();
		}
	}
}
