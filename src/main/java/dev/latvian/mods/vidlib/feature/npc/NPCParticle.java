package dev.latvian.mods.vidlib.feature.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.icon.PlumbobRenderer;
import dev.latvian.mods.vidlib.feature.particle.CustomParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NPCParticle extends CustomParticle {
	private final NPCRecording recording;
	private final PlayerProfile profile;
	private final Vec3 positionOffset;
	private final PlayerRenderer playerRenderer;
	private final RemotePlayer fakePlayer;
	private final PlayerRenderState playerRenderState;

	public NPCParticle(NPCParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		var mc = Minecraft.getInstance();
		var recordingMap = NPCRecording.getReplay(level.registryAccess());
		var recordingLazy = recordingMap.isEmpty() ? null : options.npc().equals("latest") ? recordingMap.lastEntry().getValue() : recordingMap.get(options.npc());
		this.recording = recordingLazy == null ? null : recordingLazy.get();
		this.profile = recording == null ? PlayerProfile.ERROR : PlayerProfiles.get(options.profile().orElse(recording.profile).getId());
		var modelType = mc.getModelType(profile);
		this.playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getSkinMap().get(modelType);
		this.fakePlayer = recording == null ? null : new RemotePlayer(level, profile.profile());
		this.playerRenderState = playerRenderer.createRenderState();

		if (recording != null && fakePlayer != null) {
			setLifetime(options.extraLifespan() + (int) (recording.length / 50L));
			fakePlayer.noPhysics = true;

			if (options.relativePosition()) {
				var initialPos = recording.get(NPCDataType.POSITION, 0L);
				positionOffset = new Vec3(x - initialPos.x, y - initialPos.y, z - initialPos.z);
			} else {
				positionOffset = Vec3.ZERO;
			}
		} else {
			setLifetime(0);
			positionOffset = Vec3.ZERO;
		}
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		if (recording == null || fakePlayer == null || age <= 1) {
			return;
		}

		float time = KMath.lerp(delta, prevAge, age);
		long offset = (long) (time * 50F);

		if (time >= lifetime - 1F) {
			return;
		}

		fakePlayer.snapTo(
			recording.get(NPCDataType.POSITION, offset).add(positionOffset),
			recording.get(NPCDataType.YAW, offset),
			recording.get(NPCDataType.PITCH, offset)
		);

		fakePlayer.setYBodyRot(recording.get(NPCDataType.BODY_YAW, offset));
		fakePlayer.setYHeadRot(recording.get(NPCDataType.HEAD_YAW, offset));
		fakePlayer.setOldPosAndRot();
		fakePlayer.yBodyRotO = fakePlayer.yBodyRot;
		fakePlayer.yHeadRotO = fakePlayer.yHeadRot;
		fakePlayer.setDeltaMovement(recording.get(NPCDataType.VELOCITY, offset));

		playerRenderer.extractRenderState(fakePlayer, playerRenderState, delta);

		var mc = Minecraft.getInstance();
		var eyePos = new Vec3(playerRenderState.x, playerRenderState.y + fakePlayer.getEyeHeight(), playerRenderState.z);
		var blockpos = BlockPos.containing(eyePos);
		int light = LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

		playerRenderState.skin = mc.getSkinManager().getInsecureSkin(profile.profile());
		playerRenderState.swimAmount = recording.get(NPCDataType.SWIM_AMOUNT, offset);
		playerRenderState.attackTime = recording.get(NPCDataType.ATTACK_TIME, offset);
		playerRenderState.isPassenger = recording.get(NPCDataType.PASSENGER, offset);
		playerRenderState.isUsingItem = recording.get(NPCDataType.USING_ITEM, offset);
		playerRenderState.appearsGlowing = recording.get(NPCDataType.GLOWING, offset);
		playerRenderState.hasRedOverlay = recording.get(NPCDataType.RED_OVERLAY, offset);
		playerRenderState.deathTime = recording.get(NPCDataType.DEATH_TIME, offset);

		if (playerRenderState.deathTime > 0F) {
			playerRenderState.deathTime += delta;
		}

		playerRenderState.walkAnimationPos = recording.get(NPCDataType.WALK_ANIMATION_POS, offset);
		playerRenderState.walkAnimationSpeed = recording.get(NPCDataType.WALK_ANIMATION_SPEED, offset);
		playerRenderState.feetEquipment = recording.get(NPCDataType.EQUIPMENT_FEET, offset);
		playerRenderState.legsEquipment = recording.get(NPCDataType.EQUIPMENT_LEGS, offset);
		playerRenderState.chestEquipment = recording.get(NPCDataType.EQUIPMENT_CHEST, offset);
		playerRenderState.headEquipment = recording.get(NPCDataType.EQUIPMENT_HEAD, offset);
		playerRenderState.setRenderData(VidLibEntityRenderStates.CLOTHING, recording.get(NPCDataType.CLOTHING, offset));

		playerRenderState.showHat = true;
		playerRenderState.showJacket = true;
		playerRenderState.showLeftPants = true;
		playerRenderState.showRightPants = true;
		playerRenderState.showLeftSleeve = true;
		playerRenderState.showRightSleeve = true;
		playerRenderState.showCape = true;

		var cameraPos = camera.getPosition();
		var rx = (float) (playerRenderState.x - cameraPos.x);
		var ry = (float) (playerRenderState.y - cameraPos.y);
		var rz = (float) (playerRenderState.z - cameraPos.z);

		ms.pushPose();
		ms.translate(rx, ry, rz);
		playerRenderer.render(playerRenderState, ms, buffers, light);

		var plumbob = recording.get(NPCDataType.PLUMBOB, offset);

		if (plumbob != null && plumbob != IconHolder.EMPTY) {
			PlumbobRenderer.render(mc, plumbob, eyePos, ms, delta, buffers, light, playerRenderState.isCrouching, playerRenderState.scoreText != null);
		}

		ms.popPose();
	}

	@Override
	public void tick() {
		super.tick();

		if (fakePlayer != null) {
			long offset = (long) (age * 50F);
			fakePlayer.tickCount = age;
			fakePlayer.setPose(recording.get(NPCDataType.POSE, offset));
			fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, recording.get(NPCDataType.MAIN_HAND, offset));
			fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, recording.get(NPCDataType.OFF_HAND, offset));
			fakePlayer.setHealth(recording.get(NPCDataType.HEALTH, offset));
			fakePlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(recording.get(NPCDataType.MAX_HEALTH, offset));
			fakePlayer.swinging = recording.get(NPCDataType.SWINGING, offset);

			try {
				fakePlayer.tick();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public AABB getRenderBoundingBox(float partialTicks) {
		return AABB.INFINITE;
	}
}
