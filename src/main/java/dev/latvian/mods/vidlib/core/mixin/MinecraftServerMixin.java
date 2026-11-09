package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLMinecraftServer;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements VLMinecraftServer {
	@Shadow
	@Final
	private Map<ResourceKey<Level>, ServerLevel> levels;

	@Unique
	private ServerLevel vl$overworld;

	@Unique
	private ServerSessionData vl$globalData;

	@Inject(method = "createLevels", at = @At("RETURN"))
	private void vl$createLevels(CallbackInfo ci) {
		vl$overworld = levels.get(Level.OVERWORLD);
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public final ServerLevel overworld() {
		return vl$overworld;
	}

	@Override
	@Nullable
	public ServerSessionData vl$getGlobalData() {
		if (vl$globalData == null) {
			vl$globalData = new ServerSessionData(vl$self());
			vl$globalData.load();
		}

		return vl$globalData;
	}
}
