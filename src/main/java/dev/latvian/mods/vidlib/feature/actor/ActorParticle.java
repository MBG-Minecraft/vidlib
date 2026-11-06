package dev.latvian.mods.vidlib.feature.actor;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.icon.renderer.IconRenderState;
import dev.latvian.mods.klib.icon.renderer.IconRenderer;
import dev.latvian.mods.klib.icon.renderer.IconWorldExactContext;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.vidlib.feature.clothing.ClothedPlayerSkinTexture;
import dev.latvian.mods.vidlib.feature.clothing.ClothingPresets;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.particle.CustomParticle;
import dev.latvian.mods.vidlib.feature.particle.CustomParticleState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ActorParticle extends CustomParticle {
	public static class State implements CustomParticleState {
		public double x;
		public double y;
		public double z;
		public final AvatarRenderState mannequinState = new AvatarRenderState();
		public IconRenderState plumbob = null;

		@Override
		public void submit(SubmitContext ctx) {
			ctx.push(x, y, z);
			ctx.mc().getEntityRenderDispatcher().submit(mannequinState, ctx.cameraState(), 0D, 0D, 0D, ctx.poseStack(), ctx.collector());
			ctx.pop();

			if (plumbob != null) {
				plumbob.submit(ctx);
			}
		}
	}

	@Nullable
	public static ActorParticle create(ActorParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
		var mc = Minecraft.getInstance();
		var recordingMap = ActorRecording.getReplay(level.registryAccess());
		var recordingLazy = recordingMap.isEmpty() ? null : recordingMap.get(options.mocap());
		var recording = recordingLazy == null ? null : recordingLazy.get();
		return recording == null ? null : new ActorParticle(options, mc, level, x, y, z, recording);
	}

	private final ActorRecording recording;
	private final PlayerProfile profile;
	private final Vec3 positionOffset;
	private ClientMannequin mannequin;
	public int prevAge;

	private State state;

	private ActorParticle(ActorParticleOptions options, Minecraft mc, ClientLevel level, double x, double y, double z, ActorRecording recording) {
		super(level, x, y, z);
		this.recording = recording;
		this.profile = PlayerProfiles.get(options.profile().orElse(recording.profile).id());
		setLifetime(options.extraLifespan() + (int) (recording.length / 50L));

		if (options.relativePosition()) {
			var initialPos = recording.get(ActorDataType.POSITION, 0L);
			positionOffset = new Vec3(x - initialPos.x, y - initialPos.y, z - initialPos.z);
		} else {
			positionOffset = Vec3.ZERO;
		}
	}

	@Override
	@Nullable
	public State extract(Frustum frustum, Camera camera, Vec3 cameraPos, float delta) {
		if (age <= 1) {
			return null;
		}

		float time = KMath.lerp(delta, prevAge, age);
		long offset = (long) (time * 50F);

		if (time >= lifetime - 1F) {
			return null;
		}

		var mc = Minecraft.getInstance();

		if (mannequin == null) {
			mannequin = new ClientMannequin(level, mc.playerSkinRenderCache());
			mannequin.setId(-1);
			mannequin.noPhysics = true;
		}

		mannequin.snapTo(
			recording.get(ActorDataType.POSITION, offset).add(positionOffset),
			recording.get(ActorDataType.YAW, offset),
			recording.get(ActorDataType.PITCH, offset)
		);

		mannequin.setYBodyRot(recording.get(ActorDataType.BODY_YAW, offset));
		mannequin.setYHeadRot(recording.get(ActorDataType.HEAD_YAW, offset));
		mannequin.setOldPosAndRot();
		mannequin.yBodyRotO = mannequin.yBodyRot;
		mannequin.yHeadRotO = mannequin.yHeadRot;
		mannequin.setDeltaMovement(recording.get(ActorDataType.VELOCITY, offset));

		if (state == null) {
			state = new State();
		}

		state.x = KMath.lerp(delta, mannequin.xOld, mannequin.getX());
		state.y = KMath.lerp(delta, mannequin.yOld, mannequin.getY());
		state.z = KMath.lerp(delta, mannequin.zOld, mannequin.getZ());

		var renderer = (AvatarRenderer<ClientMannequin>) mc.getEntityRenderDispatcher().getRenderer(mannequin);
		renderer.extractRenderState(mannequin, state.mannequinState, delta);

		var eyePos = new Vec3(state.mannequinState.x, state.mannequinState.y + mannequin.getEyeHeight(), state.mannequinState.z);
		var blockpos = BlockPos.containing(eyePos);
		int light = BlockUtils.getPackedLight(mc.level, blockpos);

		var skin = mc.getSkinManager().createLookup(new GameProfile(profile.profile().id(), profile.profile().name()), false).get();
		var clothing = recording.get(ActorDataType.CLOTHING, offset);

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

		state.mannequinState.skin = skin;
		state.mannequinState.swimAmount = recording.get(ActorDataType.SWIM_AMOUNT, offset);
		state.mannequinState.isPassenger = recording.get(ActorDataType.PASSENGER, offset);
		state.mannequinState.isUsingItem = recording.get(ActorDataType.USING_ITEM, offset);
		state.mannequinState.outlineColor = recording.get(ActorDataType.GLOWING, offset) ? ARGB.opaque(0xFFFFFF) : 0;
		state.mannequinState.hasRedOverlay = recording.get(ActorDataType.RED_OVERLAY, offset);
		state.mannequinState.deathTime = recording.get(ActorDataType.DEATH_TIME, offset);

		if (state.mannequinState.deathTime > 0F) {
			state.mannequinState.deathTime += delta;
		}

		state.mannequinState.walkAnimationPos = recording.get(ActorDataType.WALK_ANIMATION_POS, offset);
		state.mannequinState.walkAnimationSpeed = recording.get(ActorDataType.WALK_ANIMATION_SPEED, offset);
		state.mannequinState.feetEquipment = recording.get(ActorDataType.EQUIPMENT_FEET, offset);
		state.mannequinState.legsEquipment = recording.get(ActorDataType.EQUIPMENT_LEGS, offset);
		state.mannequinState.chestEquipment = recording.get(ActorDataType.EQUIPMENT_CHEST, offset);
		state.mannequinState.headEquipment = recording.get(ActorDataType.EQUIPMENT_HEAD, offset);

		state.mannequinState.showHat = true;
		state.mannequinState.showJacket = true;
		state.mannequinState.showLeftPants = true;
		state.mannequinState.showRightPants = true;
		state.mannequinState.showLeftSleeve = true;
		state.mannequinState.showRightSleeve = true;
		state.mannequinState.showCape = true;

		var plumbob = recording.get(ActorDataType.PLUMBOB, offset);

		if (plumbob != null && plumbob != Icon.EMPTY) {
			state.plumbob = IconRenderer.extract(plumbob, new IconWorldExactContext(mc, cameraPos, delta, 255, null));

			if (state.plumbob != null) {
				state.plumbob.x = state.x;
				state.plumbob.y = state.y + 0.52F;
				state.plumbob.z = state.z;
				state.plumbob.rotation = camera.rotation();
				state.plumbob.scale = 0.4F;
				state.plumbob.light = light;
			}
		} else {
			state.plumbob = null;
		}

		return state;
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();

		if (mannequin != null) {
			long offset = (long) (age * 50F);
			mannequin.tickCount = age;
			mannequin.setPose(recording.get(ActorDataType.POSE, offset));
			mannequin.setItemSlot(EquipmentSlot.MAINHAND, recording.get(ActorDataType.MAIN_HAND, offset));
			mannequin.setItemSlot(EquipmentSlot.OFFHAND, recording.get(ActorDataType.OFF_HAND, offset));
			mannequin.setHealth(recording.get(ActorDataType.HEALTH, offset));
			mannequin.getAttribute(Attributes.MAX_HEALTH).setBaseValue(recording.get(ActorDataType.MAX_HEALTH, offset));

			if (recording.get(ActorDataType.SWINGING, offset)) {
				mannequin.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
			}

			try {
				mannequin.tick();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
