package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.GoatRenderState;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public interface VidLibEntityRenderStates {
	ContextKey<Boolean> BOSS_FRAMEBUFFER = new ContextKey<>(VidLib.id("boss_framebuffer"));
	ContextKey<Boolean> CREATIVE = new ContextKey<>(VidLib.id("creative"));
	ContextKey<Clothing> CLOTHING = new ContextKey<>(VidLib.id("clothing"));

	ItemStack DEFAULT_SHIELD = new ItemStack(Items.SHIELD);

	static void extract(Entity entity, EntityRenderState state, float delta) {
		var mc = Minecraft.getInstance();
		var camPos = mc.gameRenderer.getMainCamera().getPosition();
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
			extractLiving(mc, camPos, e, s);
		}

		if (entity instanceof AbstractClientPlayer e && state instanceof PlayerRenderState s) {
			extractPlayer(mc, camPos, e, s);
		}

		boolean hideDetails = !VidLibConfig.entityDetailsLevelOfDetail.isVisible(camPos, state.x, state.y, state.z);
		boolean hideArmor = !(state instanceof PlayerRenderState ? VidLibConfig.playerArmorLevelOfDetail : VidLibConfig.entityArmorLevelOfDetail).isVisible(camPos, state.x, state.y, state.z);
		boolean hideHandItems = !VidLibConfig.heldItemLevelOfDetail.isVisible(camPos, state.x, state.y, state.z);
		boolean hideClothing = !VidLibConfig.clothingLevelOfDetail.isVisible(camPos, state.x, state.y, state.z);
		handleLOD(state, hideDetails, hideArmor, hideHandItems, hideClothing);
	}

	private static void handleLOD(EntityRenderState state, boolean hideDetails, boolean hideArmor, boolean hideHandItems, boolean hideClothing) {
		if (state instanceof ArmedEntityRenderState s && hideHandItems) {
			s.leftHandItem.clear();
			s.rightHandItem.clear();
		}

		if (state instanceof HumanoidRenderState s && hideArmor) {
			s.headEquipment = ItemStack.EMPTY;
			s.chestEquipment = ItemStack.EMPTY;
			s.legsEquipment = ItemStack.EMPTY;
			s.feetEquipment = ItemStack.EMPTY;
			s.headItem.clear();
		}

		if (state instanceof BeeRenderState s) {
			if (hideDetails) {
				s.hasStinger = false;
			}
		}

		if (state instanceof CatRenderState s) {
			if (hideDetails) {
				s.collarColor = null;
			}
		}

		if (state instanceof ChickenRenderState s) {
			if (hideDetails) {
				// Hide model parts mixin
			}
		}

		if (state instanceof CowRenderState s) {
			if (hideDetails) {
				// Hide model parts mixin
			}
		}

		if (state instanceof DonkeyRenderState s) {
			if (hideDetails) {
				s.hasChest = false;
			}
		}

		if (state instanceof EndermanRenderState s) {
			if (hideHandItems) {
				s.carriedBlock = null;
			}
		}

		if (state instanceof EquineRenderState s) {
			if (hideDetails) {
				s.saddle = ItemStack.EMPTY;
			}
		}

		if (state instanceof GoatRenderState s) {
			if (hideDetails) {
				s.hasLeftHorn = false;
				s.hasRightHorn = false;
			}
		}

		if (state instanceof HorseRenderState s) {
			if (hideDetails) {
				s.markings = Markings.NONE;
			}

			if (hideArmor) {
				s.bodyArmorItem = ItemStack.EMPTY;
			}
		}

		if (state instanceof ItemFrameRenderState s) {
			if (hideDetails) {
				s.item.clear();
				s.mapId = null;
				s.mapRenderState.decorations.clear();
			}
		}

		if (state instanceof LlamaRenderState s) {
			if (hideDetails) {
				s.hasChest = false;
			}

			if (hideClothing) {
				s.bodyItem = ItemStack.EMPTY;
			}
		}

		if (state instanceof PigRenderState s) {
			if (hideClothing) {
				s.saddle = ItemStack.EMPTY;
			}
		}

		if (state instanceof PlayerRenderState s) {
			if (hideDetails) {
				s.arrowCount = 0;
				s.stingerCount = 0;
			}

			if (hideClothing) {
				s.showLeftSleeve = false;
				s.showRightSleeve = false;
				s.showLeftPants = false;
				s.showRightPants = false;
				s.showJacket = false;
				s.showCape = false;
			}
		}

		if (state instanceof SkeletonRenderState s) {
			if (hideHandItems) {
				s.isHoldingBow = false;
			}
		}

		if (state instanceof VillagerRenderState s) {
			if (hideClothing) {
				s.villagerData = null;
			}
		}

		if (state instanceof WitchRenderState s) {
			if (hideHandItems) {
				s.isHoldingItem = false;
				s.isHoldingPotion = false;
			}
		}

		if (state instanceof WolfRenderState s) {
			if (hideDetails) {
				s.collarColor = null;
			}

			if (hideArmor) {
				s.bodyArmorItem = ItemStack.EMPTY;
			}
		}
	}

	static void extractLiving(Minecraft mc, Vec3 camPos, LivingEntity entity, LivingEntityRenderState state) {
		boolean bossFramebuffer = ClientGameEngine.INSTANCE.renderOnBossFramebuffer(entity);
		state.setRenderData(BOSS_FRAMEBUFFER, bossFramebuffer ? Boolean.TRUE : null);

		if (ClientGameEngine.INSTANCE.hideRenderedName(entity, bossFramebuffer)) {
			state.nameTag = null;
			state.customName = null;
		}
	}

	static void extractPlayer(Minecraft mc, Vec3 camPos, AbstractClientPlayer player, PlayerRenderState state) {
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
