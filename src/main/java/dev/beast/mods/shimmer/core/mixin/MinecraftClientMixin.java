package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftClient;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements ShimmerMinecraftClient {
	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	public abstract void setScreen(@Nullable Screen guiScreen);

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		return player.shimmer$sessionData().getScheduledTaskHandler();
	}

	@Override
	public DataMap getServerData() {
		return player.shimmer$sessionData().serverDataMap;
	}

	@Override
	public StructureStorage shimmer$structureStorage() {
		return ClientStructureStorage.CLIENT;
	}

	@Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
	private void shimmer$reloadResourcePacks(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		AutoInit.Type.ASSETS_RELOADED.invoke();
	}
}
