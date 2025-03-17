package dev.beast.mods.shimmer.feature.explosion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FakeExplosion(ServerLevel level, Vec3 pos) implements Explosion {
	@Override
	public ServerLevel level() {
		return level;
	}

	@Override
	public BlockInteraction getBlockInteraction() {
		return BlockInteraction.KEEP;
	}

	@Override
	public @Nullable LivingEntity getIndirectSourceEntity() {
		return null;
	}

	@Override
	public @Nullable Entity getDirectSourceEntity() {
		return null;
	}

	@Override
	public float radius() {
		return 0F;
	}

	@Override
	public Vec3 center() {
		return pos;
	}

	@Override
	public boolean canTriggerBlocks() {
		return false;
	}

	@Override
	public boolean shouldAffectBlocklikeEntities() {
		return false;
	}
}
