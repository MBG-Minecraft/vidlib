package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerScoreboard;
import dev.beast.mods.shimmer.feature.misc.ServerTeams;
import net.minecraft.server.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
	@Inject(method = {"onTeamAdded", "onTeamRemoved"}, at = @At("RETURN"))
	private void shimmer$onTeamAddedOrRemoved(CallbackInfo ci) {
		ServerTeams.HOLDER.update(((ShimmerScoreboard) this).shimmer$getTeams());
	}
}
