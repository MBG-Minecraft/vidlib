package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.Vec3;

public class NPCPropRenderer implements PropRenderer<NPCProp> {
	private static final PlayerSkin[] DEFAULT_SKINS = new PlayerSkin[]{
		new PlayerSkin(ID.mc("textures/entity/player/wide/steve.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/alex.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/ari.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/efe.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/kai.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/makena.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/noor.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/sunny.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/zuri.png"), null, null, null, PlayerSkin.Model.WIDE, true)
	};

	private static final PlayerSkin[] SINGLE_SKIN = {DEFAULT_SKINS[0]};

	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(NPCProp.TYPE, new NPCPropRenderer());

	public RandomSource randomSource = RandomSource.create(0L);
	private PlayerRenderer playerRenderer;
	private RemotePlayer fakePlayer;
	private PlayerRenderState playerRenderState;

	@Override
	public void render(PropRenderContext<NPCProp> ctx) {
		var p = ctx.prop();
		var ms = ctx.poseStack();
		int count = Math.max(1, p.count);
		float delta = ctx.delta();
		var buffers = ctx.frame().buffers();

		if (p.stone) {
			buffers = new MultiBufferSourceOverride(buffers, EntityRenderTypes.STONE_CUTOUT_NO_CULL, EntityRenderTypes.STONE_CUTOUT_NO_CULL);
		}

		float radius = p.spreadRadius;
		randomSource = new XoroshiroRandomSource(Double.doubleToLongBits(p.pos.x + p.id), Double.doubleToLongBits(p.pos.z + count));
		var modelType = Minecraft.getInstance().getModelType(p.profile);
		playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().get(modelType);
		playerRenderState = playerRenderer.createRenderState();

		if (fakePlayer == null) {
			fakePlayer = new RemotePlayer((ClientLevel) ctx.prop().level, p.profile);
			fakePlayer.setHealth(20F);
			fakePlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20F);
		}

		fakePlayer.vl$setLevel(ctx.prop().level);
		fakePlayer.tickCount = 0;
		fakePlayer.noPhysics = true;

		float pitch = p.getPitch(delta);
		float yaw = p.getYaw(delta);
		float roll = p.getRoll(delta);

		fakePlayer.snapTo(p.getPos(delta), yaw, pitch);
		fakePlayer.setYBodyRot(fakePlayer.getYRot());
		fakePlayer.setYHeadRot(fakePlayer.getYRot());
		fakePlayer.setOldPosAndRot();
		fakePlayer.yBodyRotO = fakePlayer.yBodyRot;
		fakePlayer.yHeadRotO = fakePlayer.yHeadRot;
		fakePlayer.setDeltaMovement(Vec3.ZERO);
		fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, p.mainHandItem);
		fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, p.offHandItem);
		fakePlayer.setPose(p.pose);
		fakePlayer.setSwimming(p.pose == Pose.SWIMMING);

		playerRenderer.extractRenderState(fakePlayer, playerRenderState, delta);

		var mc = Minecraft.getInstance();
		var eyePos = new Vec3(playerRenderState.x, playerRenderState.y + fakePlayer.getEyeHeight(), playerRenderState.z);
		var blockpos = BlockPos.containing(eyePos);
		int light = LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

		PlayerSkin[] skins;

		if (!p.profile.getName().isEmpty() && !p.profile.getId().equals(Util.NIL_UUID)) {
			skins = SINGLE_SKIN;
			skins[0] = mc.getSkinManager().getInsecureSkin(p.profile);
		} else if (p.randomSkin) {
			skins = DEFAULT_SKINS;
		} else {
			skins = SINGLE_SKIN;
			skins[0] = DEFAULT_SKINS[0];
		}

		playerRenderState.attackTime = 0;

		playerRenderState.feetEquipment = p.feetItem;
		playerRenderState.legsEquipment = p.legsItem;
		playerRenderState.chestEquipment = p.chestItem;
		playerRenderState.headEquipment = p.headItem;
		playerRenderState.setRenderData(VidLibEntityRenderStates.CLOTHING, p.clothing);

		playerRenderState.showHat = true;
		playerRenderState.showJacket = true;
		playerRenderState.showLeftPants = true;
		playerRenderState.showRightPants = true;
		playerRenderState.showLeftSleeve = true;
		playerRenderState.showRightSleeve = true;
		playerRenderState.showCape = true;
		playerRenderState.capeFlap = 0F;
		playerRenderState.capeLean = 0F;
		playerRenderState.capeLean2 = 0F;
		playerRenderState.nameTag = Empty.isEmpty(p.name) ? null : p.name;
		playerRenderState.swimAmount = p.pose == Pose.SWIMMING ? 1F : 0F;
		playerRenderState.isPassenger = p.pose == Pose.SITTING;
		playerRenderState.ageInTicks = p.breathing ? p.getTick(delta) : 0F;

		for (int i = 0; i < count; i++) {
			ms.pushPose();

			if (radius > 0F && count > 1) {
				var offset = p.spread.offset(i, count, radius);
				ms.translate(offset.x(), 0F, offset.y());
			}

			if (p.randomOffset > 0F && count > 1) {
				ms.translate(
					randomSource.nextRange(p.randomOffset),
					0F,
					randomSource.nextRange(p.randomOffset)
				);
			}

			if (p.bodyPitch != 0F) {
				ms.mulPose(Axis.XN.rotationDegrees(p.bodyPitch));
			}

			if (roll != 0F) {
				ms.mulPose(Axis.ZN.rotationDegrees(roll));
			}

			float s = (float) (p.height / 1.92D);
			ms.scale(s, s, s);

			fakePlayer.swinging = false;

			playerRenderState.yRot = 0F;
			playerRenderState.bodyRot = yaw + randomSource.nextRange(p.randomYaw);
			playerRenderState.xRot = pitch + randomSource.nextRange(p.randomPitch);
			playerRenderState.skin = skins[randomSource.nextInt(skins.length)];

			playerRenderer.render(playerRenderState, ms, buffers, light);
			ms.popPose();
		}

		fakePlayer.vl$setLevel(null);
	}
}
