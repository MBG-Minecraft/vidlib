package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLScoreboard;
import dev.latvian.mods.vidlib.feature.misc.ServerTeams;
import net.minecraft.server.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
	@Inject(method = {"onTeamAdded", "onTeamRemoved"}, at = @At("RETURN"))
	private void vl$onTeamAddedOrRemoved(CallbackInfo ci) {
		ServerTeams.REGISTRY.update(((VLScoreboard) this).vl$getTeams());
	}
}
