package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface VidLibEntityRenderStates {
	ContextKey<Boolean> MAIN_BOSS = new ContextKey<>(VidLib.id("main_boss"));
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
		var mainBoss = entity.level().getMainBoss() == entity;
		state.setRenderData(MAIN_BOSS, mainBoss ? Boolean.TRUE : null);

		if (mainBoss && !(entity instanceof Player)) {
			state.nameTag = null;
			state.customName = null;
		}
	}

	static void extractPlayer(AbstractClientPlayer player, PlayerRenderState state) {
		state.setRenderData(CREATIVE, player.isCreative() ? Boolean.TRUE : null);

		var session = player.vl$sessionData();

		var clothing = state.isInvisible ? null : player.getClothing();
		state.setRenderData(CLOTHING, clothing == Clothing.NONE ? null : clothing);

		if (state.nameTag != null) {
			state.nameTag = session.modifyPlayerName(state.nameTag);
		}

		if (session.scoreText != null) {
			state.scoreText = session.scoreText;
		}
	}

	static boolean isMainBoss(EntityRenderState state) {
		var v = state.getRenderData(MAIN_BOSS);
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
