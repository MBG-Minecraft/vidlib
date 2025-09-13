package dev.latvian.mods.vidlib.feature.prop.builtin.playerstatue;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.clothing.ClothingImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ItemStackImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.geo.BaseGeoProp;
import dev.latvian.mods.vidlib.util.SpreadType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class PlayerStatueProp extends BaseGeoProp {
	public static final Pose[] POSES = {Pose.STANDING, Pose.CROUCHING, Pose.SLEEPING, Pose.SWIMMING};

	public static final PropData<PlayerStatueProp, Component> NAME = PropData.create(PlayerStatueProp.class, "name", DataTypes.TEXT_COMPONENT, p -> p.name, (p, v) -> p.name = v, TextComponentImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Boolean> STONE = PropData.createBoolean(PlayerStatueProp.class, "stone", p -> p.stone, (p, v) -> p.stone = v);
	public static final PropData<PlayerStatueProp, GameProfile> PROFILE = PropData.create(PlayerStatueProp.class, "profile", DataTypes.GAME_PROFILE, p -> p.profile, (p, v) -> p.profile = v, GameProfileImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Integer> COUNT = PropData.createInt(PlayerStatueProp.class, "count", p -> p.count, (p, v) -> p.count = v, 1, 1000);
	public static final PropData<PlayerStatueProp, SpreadType> SPREAD = PropData.create(PlayerStatueProp.class, "spread", SpreadType.DATA_TYPE, p -> p.spread, (p, v) -> p.spread = v, () -> new EnumImBuilder<>(SpreadType.VALUES));
	public static final PropData<PlayerStatueProp, Float> BODY_PITCH = PropData.createFloat(PlayerStatueProp.class, "body_pitch", p -> p.bodyPitch, (p, v) -> p.bodyPitch = v, -90F, 90F);
	public static final PropData<PlayerStatueProp, Float> SPREAD_RADIUS = PropData.createFloat(PlayerStatueProp.class, "spread_radius", p -> p.spreadRadius, (p, v) -> p.spreadRadius = v, 0F, 200F);
	public static final PropData<PlayerStatueProp, Float> RANDOM_OFFSET = PropData.createFloat(PlayerStatueProp.class, "random_offset", p -> p.randomOffset, (p, v) -> p.randomOffset = v, 0F, 5F);
	public static final PropData<PlayerStatueProp, Float> RANDOM_YAW = PropData.createFloat(PlayerStatueProp.class, "random_yaw", p -> p.randomYaw, (p, v) -> p.randomYaw = v, 0F, 180F);
	public static final PropData<PlayerStatueProp, Float> RANDOM_HEAD_PITCH = PropData.createFloat(PlayerStatueProp.class, "random_head_pitch", p -> p.randomPitch, (p, v) -> p.randomPitch = v, 0F, 90F);
	public static final PropData<PlayerStatueProp, Clothing> CLOTHING = PropData.create(PlayerStatueProp.class, "clothing", Clothing.DATA_TYPE, p -> p.clothing, (p, v) -> p.clothing = v, ClothingImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Boolean> RANDOM_SKIN = PropData.createBoolean(PlayerStatueProp.class, "random_skin", p -> p.randomSkin, (p, v) -> p.randomSkin = v);
	public static final PropData<PlayerStatueProp, Boolean> JUMPING = PropData.createBoolean(PlayerStatueProp.class, "jumping", p -> p.jumping, (p, v) -> p.jumping = v);
	public static final PropData<PlayerStatueProp, Boolean> PUNCHING = PropData.createBoolean(PlayerStatueProp.class, "punching", p -> p.punching, (p, v) -> p.punching = v);
	public static final PropData<PlayerStatueProp, ItemStack> MAIN_HAND_ITEM = PropData.create(PlayerStatueProp.class, "main_hand_item", DataTypes.ITEM_STACK, p -> p.mainHandItem, (p, v) -> p.mainHandItem = v, ItemStackImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, ItemStack> OFF_HAND_ITEM = PropData.create(PlayerStatueProp.class, "off_hand_item", DataTypes.ITEM_STACK, p -> p.offHandItem, (p, v) -> p.offHandItem = v, ItemStackImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, ItemStack> HEAD_ITEM = PropData.create(PlayerStatueProp.class, "head_item", DataTypes.ITEM_STACK, p -> p.headItem, (p, v) -> p.headItem = v, ItemStackImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, ItemStack> CHEST_ITEM = PropData.create(PlayerStatueProp.class, "chest_item", DataTypes.ITEM_STACK, p -> p.chestItem, (p, v) -> p.chestItem = v, ItemStackImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, ItemStack> LEGS_ITEM = PropData.create(PlayerStatueProp.class, "legs_item", DataTypes.ITEM_STACK, p -> p.legsItem, (p, v) -> p.legsItem = v, ItemStackImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, ItemStack> FEET_ITEM = PropData.create(PlayerStatueProp.class, "feet_item", DataTypes.ITEM_STACK, p -> p.feetItem, (p, v) -> p.feetItem = v, ItemStackImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Pose> POSE = PropData.create(PlayerStatueProp.class, "pose", DataType.of(POSES), p -> p.pose, (p, v) -> p.pose = v, () -> new EnumImBuilder<>(POSES));

	@AutoRegister
	public static final PropType<PlayerStatueProp> TYPE = PropType.create(VidLib.id("player_statue"), PlayerStatueProp::new,
		TICK,
		POSITION,
		HEIGHT,
		YAW,
		PITCH,
		BODY_PITCH,
		ROLL,
		NAME,
		STONE,
		PROFILE,
		COUNT,
		SPREAD,
		SPREAD_RADIUS,
		RANDOM_OFFSET,
		RANDOM_YAW,
		RANDOM_HEAD_PITCH,
		CLOTHING,
		RANDOM_SKIN,
		JUMPING,
		PUNCHING,
		MAIN_HAND_ITEM,
		OFF_HAND_ITEM,
		HEAD_ITEM,
		CHEST_ITEM,
		LEGS_ITEM,
		FEET_ITEM,
		POSE
	);

	public Component name;
	public boolean stone;
	public GameProfile profile;
	public float bodyPitch;
	public int count;
	public SpreadType spread;
	public float spreadRadius;
	public float randomOffset;
	public float randomYaw;
	public float randomPitch;
	public Clothing clothing;
	public boolean randomSkin;
	public boolean jumping;
	public boolean punching;
	public ItemStack mainHandItem;
	public ItemStack offHandItem;
	public ItemStack headItem;
	public ItemStack chestItem;
	public ItemStack legsItem;
	public ItemStack feetItem;
	public Pose pose;

	public PlayerStatueProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0.6F;
		this.height = 1.8F;
		this.name = Empty.COMPONENT;
		this.gravity = 0F;
		this.stone = false;
		this.profile = Empty.PROFILE;
		this.bodyPitch = 0F;
		this.count = 1;
		this.spread = SpreadType.FILLED_SQUARE;
		this.spreadRadius = 10F;
		this.randomOffset = 0.35F;
		this.randomYaw = 0F;
		this.randomPitch = 0F;
		this.clothing = Clothing.NONE;
		this.randomSkin = false;
		this.jumping = false;
		this.punching = false;
		this.mainHandItem = ItemStack.EMPTY;
		this.offHandItem = ItemStack.EMPTY;
		this.headItem = ItemStack.EMPTY;
		this.chestItem = ItemStack.EMPTY;
		this.legsItem = ItemStack.EMPTY;
		this.feetItem = ItemStack.EMPTY;
		this.pose = Pose.STANDING;
	}

	@Override
	public double getMaxRenderDistance() {
		return Math.max(spreadRadius, 256D * height);
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		if (count > 1) {
			return true;
		}

		double s = height * 1.05D;
		return frustum.isVisible(x - s, y - s * 0.2D, z - s, x + s, y + s, z + s);
	}

	@Override
	@Nullable
	public Vec3 getInfoPos(float delta) {
		var pos = getPos(delta);
		var mat = new Matrix3f();
		mat.rotateY((float) Math.toRadians(getYaw(delta)));
		mat.rotateX((float) Math.toRadians(getPitch(delta)));
		var vec = mat.transform(new Vector3f(0F, (float) (height * 0.9D), 0F));
		return pos.add(vec.x, vec.y + height * 0.2D, vec.z);
	}

	@Override
	public Component getDisplayName() {
		return Empty.isEmpty(name) ? Component.literal(type.translationKey()) : name;
	}

	@Override
	public boolean shouldRenderDisplayName(Player to) {
		return !Empty.isEmpty(name);
	}
}
