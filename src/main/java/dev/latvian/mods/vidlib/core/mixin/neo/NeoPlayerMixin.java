package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.core.VLPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class NeoPlayerMixin extends LivingEntity implements VLPlayer {
	@Shadow
	private Component displayname;

	protected NeoPlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public void refreshDisplayName() {
		displayname = null;
	}
}
