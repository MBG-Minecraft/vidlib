package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

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

public class NPCProp extends BaseGeoProp {
	public static final Pose[] POSES = Pose.values();
	public static final Pose[] VALID_POSES = {Pose.STANDING, Pose.CROUCHING, Pose.SLEEPING, Pose.SWIMMING, Pose.SITTING};

	@AutoRegister
	public static final PropType<NPCProp> TYPE = PropType.create(VidLib.id("npc"), NPCProp::new,
		TICK,
		POSITION,
		HEIGHT,
		YAW,
		PITCH,
		ROLL,
		PropData.create(NPCProp.class, "name", DataTypes.TEXT_COMPONENT, p -> p.name, (p, v) -> p.name = v, TextComponentImBuilder.TYPE),
		PropData.createBoolean(NPCProp.class, "stone", p -> p.stone, (p, v) -> p.stone = v),
		PropData.create(NPCProp.class, "profile", DataTypes.GAME_PROFILE, p -> p.profile, (p, v) -> p.profile = v, GameProfileImBuilder.TYPE),
		PropData.createInt(NPCProp.class, "count", p -> p.count, (p, v) -> p.count = v, 1, 1000),
		PropData.create(NPCProp.class, "spread", SpreadType.DATA_TYPE, p -> p.spread, (p, v) -> p.spread = v, () -> new EnumImBuilder<>(SpreadType.VALUES)),
		PropData.createFloat(NPCProp.class, "body_pitch", p -> p.bodyPitch, (p, v) -> p.bodyPitch = v, -90F, 90F),
		PropData.createFloat(NPCProp.class, "spread_radius", p -> p.spreadRadius, (p, v) -> p.spreadRadius = v, 0F, 200F),
		PropData.createFloat(NPCProp.class, "random_offset", p -> p.randomOffset, (p, v) -> p.randomOffset = v, 0F, 5F),
		PropData.createFloat(NPCProp.class, "random_yaw", p -> p.randomYaw, (p, v) -> p.randomYaw = v, 0F, 180F),
		PropData.createFloat(NPCProp.class, "random_head_pitch", p -> p.randomPitch, (p, v) -> p.randomPitch = v, 0F, 90F),
		PropData.create(NPCProp.class, "clothing", Clothing.DATA_TYPE, p -> p.clothing, (p, v) -> p.clothing = v, ClothingImBuilder.TYPE),
		PropData.createBoolean(NPCProp.class, "random_skin", p -> p.randomSkin, (p, v) -> p.randomSkin = v),
		PropData.createBoolean(NPCProp.class, "jumping", p -> p.jumping, (p, v) -> p.jumping = v),
		PropData.createBoolean(NPCProp.class, "punching", p -> p.punching, (p, v) -> p.punching = v),
		PropData.create(NPCProp.class, "main_hand_item", DataTypes.ITEM_STACK, p -> p.mainHandItem, (p, v) -> p.mainHandItem = v, ItemStackImBuilder.TYPE),
		PropData.create(NPCProp.class, "off_hand_item", DataTypes.ITEM_STACK, p -> p.offHandItem, (p, v) -> p.offHandItem = v, ItemStackImBuilder.TYPE),
		PropData.create(NPCProp.class, "head_item", DataTypes.ITEM_STACK, p -> p.headItem, (p, v) -> p.headItem = v, ItemStackImBuilder.TYPE),
		PropData.create(NPCProp.class, "chest_item", DataTypes.ITEM_STACK, p -> p.chestItem, (p, v) -> p.chestItem = v, ItemStackImBuilder.TYPE),
		PropData.create(NPCProp.class, "legs_item", DataTypes.ITEM_STACK, p -> p.legsItem, (p, v) -> p.legsItem = v, ItemStackImBuilder.TYPE),
		PropData.create(NPCProp.class, "feet_item", DataTypes.ITEM_STACK, p -> p.feetItem, (p, v) -> p.feetItem = v, ItemStackImBuilder.TYPE),
		PropData.create(NPCProp.class, "pose", DataType.of(POSES), p -> p.pose, (p, v) -> p.pose = v, () -> new EnumImBuilder<>(VALID_POSES)),
		PropData.createBoolean(NPCProp.class, "breathing", p -> p.breathing, (p, v) -> p.breathing = v)
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
	public boolean breathing;

	public NPCProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0.6F;
		this.height = 1.8F;
		this.name = Empty.COMPONENT;
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
		this.breathing = false;
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
