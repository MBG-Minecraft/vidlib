package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerScoreboard;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Scoreboard.class)
public class ScoreboardMixin implements ShimmerScoreboard {
	@Shadow
	@Final
	private Object2ObjectMap<String, PlayerTeam> teamsByName;

	@Override
	public Map<String, PlayerTeam> shimmer$getTeams() {
		return teamsByName;
	}
}
