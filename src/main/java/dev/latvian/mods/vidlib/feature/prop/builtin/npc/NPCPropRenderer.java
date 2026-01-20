package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.gallery.PlayerSkins;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.SimplexNoise;

public class NPCPropRenderer implements PropRenderer<NPCProp> {
	private static final PlayerSkin[] SINGLE_SKIN = {PlayerSkins.DEFAULT_WIDE_SKINS[0]};

	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(NPCProp.TYPE, new NPCPropRenderer());

	private RemotePlayer fakePlayer;

	@Override
	public void render(PropRenderContext<NPCProp> ctx) {
		var mc = Minecraft.getInstance();
		var p = ctx.prop();
		var ms = ctx.poseStack();
		float delta = ctx.delta();
		var buffers = ctx.frame().buffers();
		float tick = p.getTick(delta);

		if (p.stone) {
			buffers = new MultiBufferSourceOverride(buffers, EntityRenderTypes.STONE_CUTOUT_NO_CULL, EntityRenderTypes.STONE_CUTOUT_NO_CULL);
		}

		var instances = p.getInstances();
		var profile = PlayerProfiles.get(p.profile.getId());
		var modelType = PlayerSkins.getModelType(profile);
		var playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getSkinMap().get(modelType);
		var playerRenderState = playerRenderer.createRenderState();

		if (fakePlayer == null) {
			fakePlayer = new RemotePlayer((ClientLevel) ctx.prop().level, profile.profile());
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
		fakePlayer.setItemSlot(EquipmentSlot.HEAD, p.headItem);
		fakePlayer.setItemSlot(EquipmentSlot.CHEST, p.chestItem);
		fakePlayer.setItemSlot(EquipmentSlot.LEGS, p.legsItem);
		fakePlayer.setItemSlot(EquipmentSlot.FEET, p.feetItem);
		fakePlayer.setPose(p.pose);
		fakePlayer.setSwimming(p.pose == Pose.SWIMMING);

		playerRenderer.extractRenderState(fakePlayer, playerRenderState, delta);

		var eyePos = new Vec3(playerRenderState.x, playerRenderState.y + fakePlayer.getEyeHeight(), playerRenderState.z);
		var blockpos = BlockPos.containing(eyePos);
		int light = LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

		PlayerSkin[] skins;

		if (!profile.profile().getName().isEmpty() && !profile.profile().getId().equals(Util.NIL_UUID)) {
			skins = SINGLE_SKIN;
			skins[0] = mc.getSkinManager().getInsecureSkin(profile.profile());
		} else if (p.randomSkin) {
			skins = PlayerSkins.DEFAULT_WIDE_SKINS;
		} else {
			skins = SINGLE_SKIN;
			skins[0] = PlayerSkins.DEFAULT_WIDE_SKINS[0];
		}

		playerRenderState.attackTime = 0;

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
		playerRenderState.ageInTicks = p.breathing ? tick : 0F;

		playerRenderState.walkAnimationSpeed = p.runningDistance > 0F ? 1F : 0F;

		for (int i = 0; i < instances.length; i++) {
			var npc = instances[i];
			ms.pushPose();

			float px = npc.x * p.spreadRadius;
			float py = KMath.lerp(delta, npc.prevY, npc.y);
			float pz = npc.z * p.spreadRadius;

			if (instances.length > 1) {
				px += npc.randomOffsetX * p.randomOffset;
				pz += npc.randomOffsetZ * p.randomOffset;
			}

			playerRenderState.yRot = 0F;
			playerRenderState.bodyRot = yaw + KMath.lerp(npc.randomYaw, -p.randomYaw, p.randomYaw);
			playerRenderState.xRot = pitch + KMath.lerp(npc.randomPitch, -p.randomPitch, p.randomPitch);
			playerRenderState.skin = skins[npc.profile % skins.length];
			playerRenderState.attackArm = HumanoidArm.RIGHT;
			playerRenderState.attackTime = KMath.lerp(delta, npc.prevPunching, npc.punching) / 6F;
			playerRenderState.ticksUsingItem = 0;

			if (px != 0F || py != 0F || pz != 0F) {
				ms.translate(px, py, pz);
			}

			if (p.runningDistance > 0F) {
				ms.mulPose(Axis.YP.rotationDegrees(playerRenderState.bodyRot));
				playerRenderState.bodyRot = 0F;
				var dist = (tick * 0.275D);
				playerRenderState.walkAnimationPos = (float) (dist * 3.5D) + (npc.profile % instances.length);

				float ox = SimplexNoise.noise((float) (dist * 0.3D), i * 10F) * 0.1F;
				float oz = SimplexNoise.noise((float) (dist * 0.1D), (i + instances.length) * 10F) * 0.1F;

				ms.translate(ox, 0D, (float) (dist % p.runningDistance) + oz);
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
			playerRenderer.render(playerRenderState, ms, buffers, light);
			ms.popPose();
		}

		fakePlayer.vl$setLevel(null);
	}
}
