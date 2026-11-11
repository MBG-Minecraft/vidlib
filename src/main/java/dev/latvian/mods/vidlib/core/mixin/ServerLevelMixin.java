package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.vidlib.core.VLServerLevel;
import dev.latvian.mods.vidlib.feature.misc.CreateFireworksPayload;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.ServerLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements VLServerLevel {
	@Shadow
	@Final
	private MinecraftServer server;

	@Unique
	private ServerLevelSessionData vl$levelData;

	protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Override
	public ServerLevelSessionData vl$getLevelData() {
		if (vl$levelData == null) {
			vl$levelData = ServerSessionData.of(server).getLevel(vl$self().dimension());
		}

		return vl$levelData;
	}

	@Override
	public void createFireworks(double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, List<FireworkExplosion> explosions, boolean playSound) {
		PlayerTarget.of(vl$self()).s2c(new CreateFireworksPayload(x, y, z, xSpeed, ySpeed, zSpeed, explosions));
	}

	@Inject(method = "announceSleepStatus", at = @At("HEAD"), cancellable = true)
	private void vl$announceSleepStatus(CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableSleepStatusAnnouncement(vl$self())) {
			ci.cancel();
		}
	}

	@Inject(method = "tickPrecipitation", at = @At("RETURN"))
	private void vl$tickPrecipitation(BlockPos blockPos, CallbackInfo ci, @Local(ordinal = 1) BlockPos motionBlockedPos, @Local(ordinal = 2) BlockPos belowMotionBlockedPos) {
		CommonGameEngine.INSTANCE.tickPrecipitation(vl$self(), blockPos, motionBlockedPos, belowMotionBlockedPos);
	}

	@ModifyConstant(method = {"runBlockEvents", "levelEvent"}, constant = @Constant(doubleValue = 64D))
	private double vl$eventDistance(double constant) {
		return Math.max(constant, 512D);
	}
}
