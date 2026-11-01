package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.client.SubmitContext;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.clothing.ClothedPlayerSkinTexture;
import dev.latvian.mods.vidlib.feature.clothing.ClothingPresets;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.gallery.PlayerSkins;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.SimplexNoise;

public class NPCPropRenderer implements PropRenderer<NPCProp> {
	private static final PlayerSkin[] SINGLE_SKIN = {PlayerSkins.DEFAULT_WIDE_SKINS[0]};
	// The fake player is never added to a level, so its entity id stays at the "unassigned" 0 and
	// Entity.getId throws in 26.3. Hand out a per-instance negative id (real ids are positive).
	private static final java.util.concurrent.atomic.AtomicInteger NEXT_FAKE_PLAYER_ID = new java.util.concurrent.atomic.AtomicInteger(-1);

	@ClientAutoRegister
	public static final Holder HOLDER = PropRenderer.holder(NPCProp.TYPE, NPCPropRenderer::new);

	private RemotePlayer fakePlayer;

	private static final java.util.Set<String> DEBUG_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();

	@Override
	public void render(SubmitContext<NPCProp> ctx) {
		var mc = Minecraft.getInstance();
		var p = ctx.prop();
		var ms = ctx.poseStack();
		float delta = ctx.delta();
		float tick = p.getTick(delta);

		var profile = PlayerProfiles.get(p.profile.id());
		var modelType = PlayerSkins.getModelType(profile);
		var fakeSkinState = new AvatarRenderState();
		fakeSkinState.skin = PlayerSkin.insecure(PlayerSkins.DEFAULT_WIDE_SKINS[0].body(), null, null, modelType);
		AvatarRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(fakeSkinState);

		if (fakePlayer == null) {
			fakePlayer = new RemotePlayer((ClientLevel) ctx.prop().level, new com.mojang.authlib.GameProfile(profile.profile().id(), profile.profile().name()));
			fakePlayer.setId(NEXT_FAKE_PLAYER_ID.getAndDecrement());
			fakePlayer.setHealth(20F);
			fakePlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20F);
		}

		var pos = p.getPos(delta);
		float pitch = p.getPitch(delta);
		float yaw = p.getYaw(delta);
		float roll = p.getRoll(delta);
		var pPose = p.pose;

		fakePlayer.snapTo(pos, yaw + p.additionalHeadYaw, pitch);
		fakePlayer.setYBodyRot(yaw);
		fakePlayer.setYHeadRot(yaw + p.additionalHeadYaw);
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
		fakePlayer.setPose(pPose);
		fakePlayer.setSwimming(pPose == net.minecraft.world.entity.Pose.SWIMMING);

		var playerRenderState = createState(renderer);
		extractState(renderer, fakePlayer, playerRenderState, delta);

		var eyePos = new Vec3(playerRenderState.x, playerRenderState.y + fakePlayer.getEyeHeight(), playerRenderState.z);
		var blockpos = BlockPos.containing(eyePos);
		int light = LightCoordsUtil.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

		PlayerSkin[] skins;
		var nameAndId = profile.profile();

		var pRandomSkin = p.randomSkin;
		var pRandomSkins = p.randomSkins;

		if (!nameAndId.name().isEmpty() && !nameAndId.id().equals(net.minecraft.util.Util.NIL_UUID)) {
			SINGLE_SKIN[0] = PlayerSkins.getSkin(mc, nameAndId.id(), true);
			skins = SINGLE_SKIN;
		} else if (pRandomSkin && !pRandomSkins.isEmpty()) {
			skins = pRandomSkins.stream().map(PlayerSkins::of).toArray(PlayerSkin[]::new);
		} else if (pRandomSkin) {
			skins = PlayerSkins.DEFAULT_WIDE_SKINS;
		} else {
			SINGLE_SKIN[0] = PlayerSkins.DEFAULT_WIDE_SKINS[0];
			skins = SINGLE_SKIN;
		}

		if (DEBUG_LOGGED.add(p.profile.id() + "|" + nameAndId.name())) {
			dev.latvian.mods.vidlib.VidLib.LOGGER.info("[skindbg] profile={} name='{}' uuid={} model={} body={}",
				p.profile.id(), nameAndId.name(), nameAndId.id(), skins[0].model(), skins[0].body());
		}

		var pClothing = p.clothing;

		if (ClothingPresets.ready && pClothing != PlayerClothing.NONE) {
			for (int s = 0; s < skins.length; s++) {
				var replacement = ClothedPlayerSkinTexture.replace(mc, skins[s], pClothing);

				if (replacement != null) {
					skins[s] = PlayerSkin.insecure(
						new ClientAsset.DownloadedTexture(skins[s].body().id(), replacement.toString()),
						skins[s].cape(),
						skins[s].elytra(),
						skins[s].model()
					);
				}
			}
		}

		var instances = p.getInstances();
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
		var pName = p.name;
		playerRenderState.nameTag = Empty.isEmpty(pName) ? null : pName;
		playerRenderState.swimAmount = pPose == net.minecraft.world.entity.Pose.SWIMMING ? 1F : 0F;
		playerRenderState.isPassenger = pPose == net.minecraft.world.entity.Pose.SITTING;
		playerRenderState.ageInTicks = p.breathing ? tick : 0F;
		playerRenderState.walkAnimationSpeed = p.runningDistance > 0F ? 1F : 0F;

		// Build a shuffled skin index array so no skin is ever duplicated.
		// Seeded from the prop position so it's stable across frames.
		var skinIndices = new int[instances.length];

		for (int i = 0; i < instances.length; i++) {
			skinIndices[i] = i % skins.length;
		}

		var rng = new XoroshiroRandomSource(Double.doubleToLongBits(pos.x), Double.doubleToLongBits(pos.z));

		for (int i = skinIndices.length - 1; i > 0; i--) {
			int j = (int) (rng.nextLong() % (i + 1));

			if (j < 0) {
				j += i + 1;
			}

			int tmp = skinIndices[i];
			skinIndices[i] = skinIndices[j];
			skinIndices[j] = tmp;
		}

		var collector = ctx.collector();
		var cameraState = new net.minecraft.client.renderer.state.level.CameraRenderState();
		cameraState.pos = mc.gameRenderer.mainCamera().position();
		cameraState.initialized = true;

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
			playerRenderState.skin = skins[skinIndices[i]];
			playerRenderState.mainArm = HumanoidArm.RIGHT;
			playerRenderState.swingAnimation = 0F;
			playerRenderState.ticksUsingItem = 0;

			if (px != 0F || py != 0F || pz != 0F) {
				ms.translate(px, py, pz);
			}

			if (p.runningDistance > 0F) {
				ms.rotate(Axis.YP.rotationDegrees(playerRenderState.bodyRot));
				playerRenderState.bodyRot = 0F;
				var dist = (tick * 0.275D);
				playerRenderState.walkAnimationPos = (float) (dist * 3.5D) + (npc.profile % instances.length);

				float ox = SimplexNoise.noise((float) (dist * 0.3D), i * 10F) * 0.1F;
				float oz = SimplexNoise.noise((float) (dist * 0.1D), (i + instances.length) * 10F) * 0.1F;

				ms.translate(ox, 0D, (float) (dist % p.runningDistance) + oz);
			}

			if (p.bodyPitch != 0F) {
				ms.rotate(Axis.XN.rotationDegrees(p.bodyPitch));
			}

			if (roll != 0F) {
				ms.rotate(Axis.ZN.rotationDegrees(roll));
			}

			float s = (float) (p.height / 1.92D);
			ms.scale(s, s, s);

			submit(renderer, playerRenderState, ms, collector, cameraState);
			ms.popPose();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static AvatarRenderState createState(AvatarRenderer<?> renderer) {
		return (AvatarRenderState) renderer.createRenderState();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void extractState(AvatarRenderer<?> renderer, RemotePlayer player, AvatarRenderState state, float delta) {
		((AvatarRenderer) renderer).extractRenderState((AbstractClientPlayer) (Object) player, state, delta);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void submit(AvatarRenderer<?> renderer, AvatarRenderState state, com.mojang.blaze3d.vertex.PoseStack ms, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
		var poseStack = (com.mojang.blaze3d.vertex.PoseStack) ms;
		renderer.submit(state, poseStack, collector, camera);
	}
}
