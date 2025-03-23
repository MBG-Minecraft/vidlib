package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerClientLevel;
import dev.beast.mods.shimmer.feature.prop.ClientPropList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements ShimmerClientLevel {
	@Unique
	private ClientPropList shimmer$props;

	@Shadow
	protected abstract LevelEntityGetter<Entity> getEntities();

	@Override
	public ClientPropList getProps() {
		if (shimmer$props == null) {
			shimmer$props = new ClientPropList(shimmer$level());
		}

		return shimmer$props;
	}

	@Override
	@Nullable
	public Entity getEntityByUUID(UUID uuid) {
		return getEntities().get(uuid);
	}
}
