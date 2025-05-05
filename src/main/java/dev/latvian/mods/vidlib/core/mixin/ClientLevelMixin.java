package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientLevel;
import dev.latvian.mods.vidlib.feature.prop.ClientPropList;
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

import java.util.UUID;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements VLClientLevel {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private ClientPropList vl$props;

	@Shadow
	protected abstract LevelEntityGetter<Entity> getEntities();

	@Override
	public ClientPropList getProps() {
		if (vl$props == null) {
			vl$props = new ClientPropList(this.vl$level());
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
		environmentEffects(minecraft, blockPos);
	}

	@Redirect(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(ClientLevel level, BlockPos pos) {
		return level.vl$overrideFluidState(pos);
	}
}
