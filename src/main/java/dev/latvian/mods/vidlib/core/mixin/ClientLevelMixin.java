package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientLevel;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.FluidState;
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

import java.util.UUID;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends LevelMixin implements VLClientLevel {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private ClientProps vl$props;

	@Shadow
	protected abstract LevelEntityGetter<Entity> getEntities();

	@Shadow
	private boolean tickDayTime;

	@Override
	public ClientProps getProps() {
		if (vl$props == null || vl$props.level != vl$level()) {
			vl$props = new ClientProps(vl$level());
		}

		return vl$props;
	}

	@Override
	@Nullable
	public Entity getEntityByUUID(UUID uuid) {
		return getEntities().get(uuid);
	}

	@Inject(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private void vl$doAnimateTick(int posX, int posY, int posZ, int range, RandomSource random, Block block, BlockPos.MutableBlockPos blockPos, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.handleEnvironmentalEffects(minecraft, vl$level(), blockPos);
	}

	@Redirect(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(ClientLevel level, BlockPos pos) {
		return level.vl$overrideFluidState(pos);
	}

	@Override
	public boolean vl$getTickDayTime() {
		return tickDayTime;
	}

	@Inject(method = "gatherChunkSourceStats", at = @At("RETURN"), cancellable = true)
	private void gatherChunkSourceStats(CallbackInfoReturnable<String> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue("");
		}
	}

	@Override
	public KNumberContext getGlobalContext() {
		var v = super.getGlobalContext();
		v.entity = minecraft.player;
		return v;
	}
}
