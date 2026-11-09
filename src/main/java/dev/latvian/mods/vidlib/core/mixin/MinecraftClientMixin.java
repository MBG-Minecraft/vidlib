package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLMinecraftClient;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import dev.latvian.mods.vidlib.feature.misc.MainMenuOpenedEvent;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.progressqueue.BlockExitScreen;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements VLMinecraftClient {
	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Unique
	private int vl$reloadCount = 0;

	@Unique
	private ClientSessionData vl$globalData;

	@Override
	public ClientSessionData vl$getGlobalData() {
		if (vl$globalData == null) {
			vl$globalData = ClientSessionData.of(vl$self());
		}

		return vl$globalData;
	}

	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private void vl$swap(ClientPacketListener instance, Packet packet) {
		if (!PlayerActionHandler.handle(player, PlayerActionType.SWAP, true)) {
			instance.send(packet);
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void vl$startAttack(CallbackInfoReturnable<Boolean> cir) {
		if (PlayerActionHandler.handle(player, PlayerActionType.ATTACK, true)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void vl$continueAttack(boolean leftClick, CallbackInfo ci) {
		if (PlayerActionHandler.handle(player, PlayerActionType.ATTACK, false)) {
			ci.cancel();
		}
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void vl$startUseItem(CallbackInfo ci) {
		if (PlayerActionHandler.handle(player, PlayerActionType.INTERACT, true)) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 1))
	private boolean vl$handleKeybindsUsingItem(boolean original) {
		return original || PlayerActionHandler.handle(player, PlayerActionType.INTERACT, false);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(Lcom/mojang/renderpearl/api/device/GpuDevice;)V", shift = At.Shift.AFTER))
	public void vl$initRenderer(GameConfig gameConfig, CallbackInfo ci) {
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	public void vl$onFinishInit(CallbackInfo ci) {
		ImGuiHooks.init(vl$self(), resourceManager);
	}

	@Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getNanos()J", ordinal = 0))
	public void vl$onStartFrame(boolean tick, CallbackInfo ci) {
		ImGuiHooks.startFrame(vl$self());
	}

	// 26.3 moved GpuSurface#present() ahead of the last Util#getNanos() call site, so the UI has to
	// be built right after the game image is blitted onto the window surface...
	@Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;blitFromTexture(Lcom/mojang/renderpearl/api/commands/CommandEncoder;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V", shift = At.Shift.AFTER))
	public void vl$beforeEndFrame(boolean tick, CallbackInfo ci) {
		ImGuiHooks.beforeEndFrame(vl$self());
	}

	// ...and drawn right before it is presented, otherwise the whole ImGui overlay is composited
	// after the swap and never reaches the screen.
	@Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;present()V", shift = At.Shift.BEFORE))
	public void vl$onEndFrame(boolean tick, CallbackInfo ci) {
		ImGuiHooks.endFrame(vl$self());
	}

	@Inject(method = "emergencySave", at = @At("HEAD"))
	public void vl$onCrashCleanup(CallbackInfo ci) {
		ImGuiHooks.ensureEndFrame();
	}

	@Inject(method = "run", at = @At("RETURN"))
	public void vl$onEndGameLoop(CallbackInfo ci) {
		ImGuiHooks.ensureEndFrame();
	}

	@Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
	private void vl$isCurrentlyGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.isGlowing(vl$self(), entity)) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public int vl$reloadCount() {
		return vl$reloadCount;
	}

	@Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
	private void vl$reloadResourcePacks(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		vl$reloadCount++;
	}

	@Inject(method = "onGameLoadFinished", at = @At("RETURN"))
	private void vl$onGameLoadFinished(CallbackInfo ci) {
		NeoForge.EVENT_BUS.post(new MainMenuOpenedEvent(vl$self(), true));
	}

	@WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;stop()V"))
	private void vl$stop(Minecraft mc, Operation<Void> original, @Local(argsOnly = true) boolean advanceGameTime) {
		if (BlockExitScreen.preventExit(mc, advanceGameTime)) {
			// 26.3 removed the SDL window-close helper; Window#shouldClose is a latched flag, so
			// simply not stopping here keeps the close request alive until we can actually exit.
		} else {
			original.call(mc);
		}
	}
}
