package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerClientLevel;
import dev.beast.mods.shimmer.feature.prop.ClientPropList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements ShimmerClientLevel {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private ClientPropList shimmer$props;

	@Shadow
	protected abstract LevelEntityGetter<Entity> getEntities();

	@Override
	public ClientPropList getProps() {
		if (shimmer$props == null) {
			shimmer$props = new ClientPropList(this.shimmer$level());
		}

		return shimmer$props;
	}

	@Override
	@Nullable
	public Entity getEntityByUUID(UUID uuid) {
		return getEntities().get(uuid);
	}

	@Inject(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private void shimmer$doAnimateTick(int posX, int posY, int posZ, int range, RandomSource random, Block block, BlockPos.MutableBlockPos blockPos, CallbackInfo ci) {
		environmentEffects(minecraft, blockPos);
	}
}
