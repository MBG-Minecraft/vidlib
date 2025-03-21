package dev.beast.mods.shimmer.feature.net;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShimmerPayloadContext(IPayloadContext parent, Level level, Player player, long uid, long remoteGameTime) {
	public ShimmerPayloadContext(IPayloadContext ctx, long uid, long remoteGameTime) {
		this(ctx, ctx.player().level(), ctx.player(), uid, remoteGameTime);
	}
}
