package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface VidLibEntityRenderStates {
	ContextKey<Boolean> BOSS_FRAMEBUFFER = new ContextKey<>(VidLib.id("boss_framebuffer"));
	ContextKey<Boolean> CREATIVE = new ContextKey<>(VidLib.id("creative"));
	ContextKey<Clothing> CLOTHING = new ContextKey<>(VidLib.id("clothing"));

	static void extract(Entity entity, EntityRenderState state, float delta) {
		var vehicle = entity.getVehicle();

		if (vehicle != null) {
			var s = vehicle.getPassengerScale(entity);

			if (s <= 0F) {
				state.isInvisible = true;
			} else if (state instanceof LivingEntityRenderState livingState && s > 0F) {
				livingState.scale *= s;
			}
		}

		if (entity instanceof LivingEntity e && state instanceof LivingEntityRenderState s) {
			extractLiving(e, s);
		}

		if (entity instanceof AbstractClientPlayer e && state instanceof PlayerRenderState s) {
			extractPlayer(e, s);
		}
	}

	static void extractLiving(LivingEntity entity, LivingEntityRenderState state) {
		boolean bossFramebuffer = ClientGameEngine.INSTANCE.renderOnBossFramebuffer(entity);
		state.setRenderData(BOSS_FRAMEBUFFER, bossFramebuffer ? Boolean.TRUE : null);

		if (ClientGameEngine.INSTANCE.hideRenderedName(entity, bossFramebuffer)) {
			state.nameTag = null;
			state.customName = null;
		}
	}

	static void extractPlayer(AbstractClientPlayer player, PlayerRenderState state) {
		state.setRenderData(CREATIVE, player.isCreative() ? Boolean.TRUE : null);

		var clothing = state.isInvisible ? null : ClientGameEngine.INSTANCE.getClothing(player);
		state.setRenderData(CLOTHING, clothing == Clothing.NONE ? null : clothing);

		if (state.nameTag != null) {
			state.nameTag = ClientGameEngine.INSTANCE.getFullPlayerWorldName(player, state.nameTag);
		}

		var scoreText = ClientGameEngine.INSTANCE.getScoreText(player);

		if (scoreText != null) {
			state.scoreText = Empty.isEmpty(scoreText) ? null : scoreText;
		}
	}

	static boolean isMainBoss(EntityRenderState state) {
		var v = state.getRenderData(BOSS_FRAMEBUFFER);
		return v != null && v;
	}

	static boolean isCreative(PlayerRenderState state) {
		var v = state.getRenderData(CREATIVE);
		return v != null && v;
	}

	static Clothing getClothing(EntityRenderState state) {
		var v = state.getRenderData(CLOTHING);
		return v == null ? Clothing.NONE : v;
	}
}
