package dev.latvian.mods.vidlib.feature.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record Context(IPayloadContext parent, ResourceLocation type, Level level, Player player, long uid, long remoteGameTime) {
	public Context(IPayloadContext ctx, ResourceLocation type, long uid, long remoteGameTime) {
		this(ctx, type, ctx.player().level(), ctx.player(), uid, remoteGameTime);
	}

	public RandomSource createRandom() {
		return new XoroshiroRandomSource(uid, remoteGameTime);
	}

	public long getSeed() {
		return Long.rotateLeft(uid + remoteGameTime, 17) + uid;
	}
}
