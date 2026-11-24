package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import dev.latvian.mods.vidlib.core.api.client.VLClientLevel;
import dev.latvian.mods.vidlib.core.mixin.common.LevelMixin;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionDataEnvironmentAttributeSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends LevelMixin implements VLClientLevel {
	@Shadow
	@Final
	public Minecraft minecraft;

	@Shadow
	@Final
	private EnvironmentAttributeSystem environmentAttributes;

	@Unique
	private ClientLevelSessionData vl$levelData;

	@Unique
	private EnvironmentAttributeSystem vl$environmentAttributes;

	@Override
	public ClientLevelSessionData vl$getLevelData() {
		if (vl$levelData == null) {
			vl$levelData = ClientSessionData.of(minecraft).getLevel(vl$self().dimension());
		}

		return vl$levelData;
	}

	@Override
	public void vl$updateAttributes() {
		if (vl$environmentAttributes == null) {
			vl$environmentAttributes = new LevelSessionDataEnvironmentAttributeSystem(environmentAttributes, vl$getLevelData());
		}
	}

	@Inject(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
	private void vl$doAnimateTick(int posX, int posY, int posZ, int range, RandomSource random, Block block, BlockPos.MutableBlockPos blockPos, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.handleEnvironmentalEffects(minecraft, vl$self(), blockPos);
	}

	@WrapOperation(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(ClientLevel level, BlockPos pos, Operation<FluidState> original) {
		return VLBridge.overrideFluidState.apply(level, pos);
	}

	@Inject(method = "gatherChunkSourceStats", at = @At("RETURN"), cancellable = true)
	private void gatherChunkSourceStats(CallbackInfoReturnable<String> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue("");
		}
	}

	@ModifyExpressionValue(method = "environmentAttributes()Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ClientLevel;environmentAttributes:Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;", opcode = Opcodes.GETFIELD))
	private EnvironmentAttributeSystem vl$environmentAttributes(EnvironmentAttributeSystem original) {
		return vl$environmentAttributes == null ? original : vl$environmentAttributes;
	}
}
