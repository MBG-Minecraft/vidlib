package dev.latvian.mods.vidlib.feature.npc;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.clothing.ClothedPlayerSkinTexture;
import dev.latvian.mods.vidlib.feature.clothing.ClothingPresets;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.gallery.PlayerSkins;
import dev.latvian.mods.vidlib.feature.icon.PlumbobRenderer;
import dev.latvian.mods.vidlib.feature.particle.CustomParticle;
import dev.latvian.mods.vidlib.feature.particle.ParticleGeometryBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class NPCParticle extends CustomParticle {
	private final NPCRecording recording;
	private final PlayerProfile profile;
	private final Vec3 positionOffset;
	private final AvatarRenderer<?> playerRenderer;
	private final RemotePlayer fakePlayer;
	private final AvatarRenderState playerRenderState;

	public NPCParticle(NPCParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		var mc = Minecraft.getInstance();
		var recordingMap = NPCRecording.getReplay(level.registryAccess());
		var recordingLazy = recordingMap.isEmpty() ? null : recordingMap.get(options.npc());
		this.recording = recordingLazy == null ? null : recordingLazy.get();
		this.profile = recording == null ? PlayerProfile.ERROR : PlayerProfiles.get(options.profile().orElse(recording.profile).id());
		var modelType = PlayerSkins.getModelType(profile);
		this.playerRenderer = vl$avatarRenderer(mc, modelType);
		this.fakePlayer = recording == null ? null : new RemotePlayer(level, new GameProfile(profile.profile().id(), profile.profile().name()));
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

	/**
	 * 1.21.5 looked the renderer up in {@code EntityRenderDispatcher#getSkinMap()}; 26.3 keys the
	 * avatar renderers off the model type inside {@link AvatarRenderState}.
	 */
	private static AvatarRenderer<?> vl$avatarRenderer(Minecraft mc, net.minecraft.world.entity.player.PlayerModelType modelType) {
		var probe = new AvatarRenderState();
		probe.skin = new PlayerSkin(DefaultPlayerSkin.getDefaultSkin().body(), null, null, modelType, true);
		return mc.getEntityRenderDispatcher().getRenderer(probe);
	}

	@Override
	public void renderCustom(com.mojang.blaze3d.vertex.PoseStack ms, ParticleGeometryBuffers buffers, Camera camera, float delta) {
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

		@SuppressWarnings("unchecked")
		AvatarRenderer<RemotePlayer> renderer = (AvatarRenderer<RemotePlayer>) (AvatarRenderer<?>) playerRenderer;
		renderer.extractRenderState(fakePlayer, playerRenderState, delta);

		var mc = Minecraft.getInstance();
		var eyePos = new Vec3(playerRenderState.x, playerRenderState.y + fakePlayer.getEyeHeight(), playerRenderState.z);
		var blockpos = BlockPos.containing(eyePos);
		int light = LightCoordsUtil.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

		var skin = mc.getSkinManager().createLookup(new GameProfile(profile.profile().id(), profile.profile().name()), false).get();
		var clothing = recording.get(NPCDataType.CLOTHING, offset);

		if (ClothingPresets.ready && clothing != PlayerClothing.NONE) {
			var replacement = ClothedPlayerSkinTexture.replace(mc, skin, clothing);

			if (replacement != null) {
				skin = new PlayerSkin(
					new net.minecraft.core.ClientAsset.ResourceTexture(replacement, replacement),
					skin.cape(),
					skin.elytra(),
					skin.model(),
					true
				);
			}
		}

		playerRenderState.skin = skin;
		playerRenderState.swimAmount = recording.get(NPCDataType.SWIM_AMOUNT, offset);
		playerRenderState.isPassenger = recording.get(NPCDataType.PASSENGER, offset);
		playerRenderState.isUsingItem = recording.get(NPCDataType.USING_ITEM, offset);
		playerRenderState.outlineColor = recording.get(NPCDataType.GLOWING, offset) ? ARGB.opaque(0xFFFFFF) : 0;
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

		playerRenderState.showHat = true;
		playerRenderState.showJacket = true;
		playerRenderState.showLeftPants = true;
		playerRenderState.showRightPants = true;
		playerRenderState.showLeftSleeve = true;
		playerRenderState.showRightSleeve = true;
		playerRenderState.showCape = true;

		var cameraPos = camera.position();
		var rx = (float) (playerRenderState.x - cameraPos.x);
		var ry = (float) (playerRenderState.y - cameraPos.y);
		var rz = (float) (playerRenderState.z - cameraPos.z);

		ms.pushPose();
		ms.translate(rx, ry, rz);
		renderer.submit(playerRenderState, ms, buffers.collector(), cameraRenderState == null ? new CameraRenderState() : cameraRenderState);

		var plumbob = recording.get(NPCDataType.PLUMBOB, offset);

		if (plumbob != null && plumbob != Icon.EMPTY) {
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

			if (recording.get(NPCDataType.SWINGING, offset)) {
				fakePlayer.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
			}

			try {
				fakePlayer.tick();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
