package dev.latvian.mods.vidlib.core.api.common;

import net.minecraft.world.entity.LivingEntity;

public interface VLLivingEntity extends VLEntity {
	@Override
	default LivingEntity vl$self() {
		return (LivingEntity) this;
	}
}
