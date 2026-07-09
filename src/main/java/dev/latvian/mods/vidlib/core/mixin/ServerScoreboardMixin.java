package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLScoreboard;
import dev.latvian.mods.vidlib.feature.misc.ServerTeams;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = {"onTeamAdded", "onTeamRemoved"}, at = @At("RETURN"))
	private void vl$onTeamAddedOrRemoved(CallbackInfo ci) {
		ServerTeams.update(server, (VLScoreboard) this);
	}
}
