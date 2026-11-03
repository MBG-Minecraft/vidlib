package dev.latvian.mods.vidlib.feature.prop.builtin.mannequin;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.icon.renderer.IconRenderState;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.clothing.ClothedPlayerSkinTexture;
import dev.latvian.mods.vidlib.feature.clothing.ClothingPresets;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.gallery.PlayerSkins;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.SimplexNoise;

public class MannequinPropRenderState extends PropRenderState<MannequinProp> {
	private static final PlayerSkin[] SINGLE_SKIN = {PlayerSkins.DEFAULT_WIDE_SKINS[0]};

	public static class InstanceState {
		public final Matrix4f transform = new Matrix4f();
		public final AvatarRenderState mannequinState = new AvatarRenderState();
	}

	public double x;
	public double y;
	public double z;
	public ClientMannequin mannequin;
	public IconRenderState plumbob = null;
	public InstanceState[] instanceStates = new InstanceState[0];

	@Override
	public void extract(MannequinProp p, VidLibExtractContext ctx) {
		var mc = ctx.mc();
		float delta = ctx.delta();
		float tick = p.getTick(delta);

		var profile = PlayerProfiles.get(p.profile.id());
		// var mannequin = new AvatarRenderState();
		// mannequin.skin = PlayerSkin.insecure(PlayerSkins.DEFAULT_WIDE_SKINS[0].body(), null, null, modelType);

		if (mannequin == null) {
			mannequin = new ClientMannequin(p.level, mc.playerSkinRenderCache());
			mannequin.setId(-1);
		}

		var pos = p.getPos(delta);
		float pitch = p.getPitch(delta);
		float yaw = p.getYaw(delta);
		float roll = p.getRoll(delta);
		var pPose = p.pose;

		mannequin.snapTo(pos, yaw + p.additionalHeadYaw, pitch);
		mannequin.setYBodyRot(yaw);
		mannequin.setYHeadRot(yaw + p.additionalHeadYaw);
		mannequin.setOldPosAndRot();
		mannequin.yBodyRotO = mannequin.yBodyRot;
		mannequin.yHeadRotO = mannequin.yHeadRot;
		mannequin.setDeltaMovement(Vec3.ZERO);
		mannequin.setItemSlot(EquipmentSlot.MAINHAND, p.mainHandItem);
		mannequin.setItemSlot(EquipmentSlot.OFFHAND, p.offHandItem);
		mannequin.setItemSlot(EquipmentSlot.HEAD, p.headItem);
		mannequin.setItemSlot(EquipmentSlot.CHEST, p.chestItem);
		mannequin.setItemSlot(EquipmentSlot.LEGS, p.legsItem);
		mannequin.setItemSlot(EquipmentSlot.FEET, p.feetItem);
		mannequin.setPose(pPose);
		mannequin.setSwimming(pPose == net.minecraft.world.entity.Pose.SWIMMING);

		var instances = p.getInstances();

		if (instances.length != instanceStates.length) {
			instanceStates = new InstanceState[instances.length];

			for (int i = 0; i < instances.length; i++) {
				instanceStates[i] = new InstanceState();
			}
		}

		var renderer = (AvatarRenderer<ClientMannequin>) mc.getEntityRenderDispatcher().getRenderer(mannequin);

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

		for (var i = 0; i < instanceStates.length; i++) {
			var instance = instances[i];
			var istate = instanceStates[i];
			var mannequinState = istate.mannequinState;

			renderer.extractRenderState(mannequin, istate.mannequinState, delta);

			var eyePos = new Vec3(mannequinState.x, mannequinState.y + mannequin.getEyeHeight(), mannequinState.z);
			var blockpos = BlockPos.containing(eyePos);
			int light = LightCoordsUtil.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

			mannequinState.showHat = true;
			mannequinState.showJacket = true;
			mannequinState.showLeftPants = true;
			mannequinState.showRightPants = true;
			mannequinState.showLeftSleeve = true;
			mannequinState.showRightSleeve = true;
			mannequinState.showCape = true;
			mannequinState.capeFlap = 0F;
			mannequinState.capeLean = 0F;
			mannequinState.capeLean2 = 0F;
			var pName = p.name;
			mannequinState.nameTag = Empty.isEmpty(pName) ? null : pName;
			mannequinState.swimAmount = pPose == net.minecraft.world.entity.Pose.SWIMMING ? 1F : 0F;
			mannequinState.isPassenger = pPose == net.minecraft.world.entity.Pose.SITTING;
			mannequinState.ageInTicks = p.breathing ? tick : 0F;
			mannequinState.walkAnimationSpeed = p.runningDistance > 0F ? 1F : 0F;

			float px = instance.x * p.spreadRadius;
			float py = KMath.lerp(delta, instance.prevY, instance.y);
			float pz = instance.z * p.spreadRadius;

			if (instances.length > 1) {
				px += instance.randomOffsetX * p.randomOffset;
				pz += instance.randomOffsetZ * p.randomOffset;
			}

			mannequinState.yRot = 0F;
			mannequinState.bodyRot = yaw + KMath.lerp(instance.randomYaw, -p.randomYaw, p.randomYaw);
			mannequinState.xRot = pitch + KMath.lerp(instance.randomPitch, -p.randomPitch, p.randomPitch);
			mannequinState.skin = skins[skinIndices[instance.index]];
			mannequinState.mainArm = HumanoidArm.RIGHT;
			mannequinState.swingAnimation = 0F;
			mannequinState.ticksUsingItem = 0;

			istate.transform.identity();

			if (px != 0F || py != 0F || pz != 0F) {
				istate.transform.translate(px, py, pz);
			}

			if (p.runningDistance > 0F) {
				istate.transform.rotate(Axis.YP.rotationDegrees(mannequinState.bodyRot));
				mannequinState.bodyRot = 0F;
				var dist = (tick * 0.275D);
				mannequinState.walkAnimationPos = (float) (dist * 3.5D) + (instance.profile % instances.length);

				float ox = SimplexNoise.noise((float) (dist * 0.3D), i * 10F) * 0.1F;
				float oz = SimplexNoise.noise((float) (dist * 0.1D), (i + instances.length) * 10F) * 0.1F;

				istate.transform.translate(ox, 0F, (float) (dist % p.runningDistance) + oz);
			}

			if (p.bodyPitch != 0F) {
				istate.transform.rotate(Axis.XN.rotationDegrees(p.bodyPitch));
			}

			if (roll != 0F) {
				istate.transform.rotate(Axis.ZN.rotationDegrees(roll));
			}

			float s = (float) (p.height / 1.92D);
			istate.transform.scale(s, s, s);
		}
	}

	@Override
	public void submit(SubmitContext ctx) {
		ctx.push(x, y, z);
		var poseStack = ctx.poseStack();

		for (var istate : instanceStates) {
			poseStack.pushPose();
			poseStack.mulPose(istate.transform);
			ctx.mc().getEntityRenderDispatcher().submit(istate.mannequinState, ctx.cameraState(), 0D, 0D, 0D, poseStack, ctx.collector());
			poseStack.popPose();
		}

		ctx.pop();

		if (plumbob != null) {
			plumbob.submit(ctx);
		}
	}
}
