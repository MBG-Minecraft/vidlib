package dev.latvian.mods.vidlib.feature.prop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class PropEntity extends Entity {
	public final Prop prop;

	public PropEntity(Prop prop) {
		super(EntityType.BLOCK_DISPLAY, prop.level);
		this.prop = prop;
	}

	@Override
	public Component getDisplayName() {
		return prop.getDisplayName();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}
}
