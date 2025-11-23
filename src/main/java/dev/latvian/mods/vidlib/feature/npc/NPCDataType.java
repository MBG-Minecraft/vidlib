package dev.latvian.mods.vidlib.feature.npc;

import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.SimilarityCheck;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public record NPCDataType<T>(String name, StreamCodec<?, T> streamCodec, T defaultValue, SimilarityCheck<T> similarityCheck, NPCValueGetter<T> getter) implements StringRepresentable {
	public static final Map<String, NPCDataType<?>> MAP = new Object2ObjectOpenHashMap<>();

	public static <T> NPCDataType<T> add(String name, StreamCodec<?, T> streamCodec, T defaultValue, SimilarityCheck<T> similarityCheck, NPCValueGetter<T> getter) {
		var type = new NPCDataType<>(name, streamCodec, defaultValue, similarityCheck, getter);
		MAP.put(name, type);
		return type;
	}

	public static NPCDataType<Integer> addInt(String name, int defaultValue, ToIntFunction<Player> getter) {
		return add(name, ByteBufCodecs.VAR_INT, defaultValue, SimilarityCheck.getDefault(), (player, delta) -> getter.applyAsInt(player));
	}

	public static NPCDataType<Float> addFloat(String name, float defaultValue, NPCValueGetter<Float> getter) {
		return add(name, ByteBufCodecs.FLOAT, defaultValue, SimilarityCheck.FLOAT, getter);
	}

	public static NPCDataType<Boolean> addBoolean(String name, boolean defaultValue, Predicate<Player> getter) {
		return add(name, ByteBufCodecs.BOOL, defaultValue, SimilarityCheck.getDefault(), (player, delta) -> getter.test(player));
	}

	public static NPCDataType<ItemStack> addEquipment(String name, EquipmentSlot slot) {
		return add(name, ItemStack.OPTIONAL_STREAM_CODEC, ItemStack.EMPTY, SimilarityCheck.ITEM_STACK, (player, delta) -> player.getItemBySlot(slot).copy());
	}

	public static final NPCDataType<Vec3> POSITION = add("position", MCStreamCodecs.VEC3, Vec3.ZERO, SimilarityCheck.VEC3, Entity::getPosition);
	public static final NPCDataType<Float> YAW = addFloat("yaw", 0F, Entity::getViewYRot);
	public static final NPCDataType<Float> PITCH = addFloat("pitch", 0F, Entity::getViewXRot);
	public static final NPCDataType<Float> BODY_YAW = addFloat("body_yaw", 0F, (p, d) -> Mth.lerp(d, p.yBodyRotO, p.yBodyRot));
	public static final NPCDataType<Float> HEAD_YAW = addFloat("head_yaw", 0F, (p, d) -> Mth.lerp(d, p.yHeadRotO, p.yHeadRot));
	public static final NPCDataType<Vec3> VELOCITY = add("velocity", MCStreamCodecs.VEC3S, Vec3.ZERO, SimilarityCheck.VEC3, (p, d) -> p.getDeltaMovement());
	public static final NPCDataType<ItemStack> MAIN_HAND = addEquipment("equipment/hand/main", EquipmentSlot.MAINHAND);
	public static final NPCDataType<ItemStack> OFF_HAND = addEquipment("equipment/hand/off", EquipmentSlot.OFFHAND);
	public static final NPCDataType<ItemStack> EQUIPMENT_FEET = addEquipment("equipment/feet", EquipmentSlot.FEET);
	public static final NPCDataType<ItemStack> EQUIPMENT_LEGS = addEquipment("equipment/legs", EquipmentSlot.LEGS);
	public static final NPCDataType<ItemStack> EQUIPMENT_CHEST = addEquipment("equipment/chest", EquipmentSlot.CHEST);
	public static final NPCDataType<ItemStack> EQUIPMENT_HEAD = addEquipment("equipment/head", EquipmentSlot.HEAD);
	public static final NPCDataType<Float> HEALTH = addFloat("health", 20F, (p, d) -> p.getHealth());
	public static final NPCDataType<Float> MAX_HEALTH = addFloat("max_health", 20F, (p, d) -> p.getMaxHealth());
	public static final NPCDataType<Pose> POSE = add("pose", Pose.STREAM_CODEC, Pose.STANDING, SimilarityCheck.getDefault(), (p, d) -> p.getPose());
	public static final NPCDataType<Boolean> SWINGING = addBoolean("swinging", false, p -> p.swinging);
	public static final NPCDataType<Float> SWIM_AMOUNT = addFloat("swim_amount", 0F, LivingEntity::getSwimAmount);
	public static final NPCDataType<Float> ATTACK_TIME = addFloat("attack_time", 0F, LivingEntity::getAttackAnim);
	public static final NPCDataType<Boolean> PASSENGER = addBoolean("passenger", false, p -> p.isPassenger() && (p.getVehicle() != null && p.getVehicle().shouldRiderSit()));
	public static final NPCDataType<Boolean> USING_ITEM = addBoolean("using_item", false, LivingEntity::isUsingItem);
	public static final NPCDataType<Boolean> GLOWING = addBoolean("glowing", false, Entity::isCurrentlyGlowing);
	public static final NPCDataType<Boolean> RED_OVERLAY = addBoolean("red_overlay", false, p -> p.hurtTime > 0 || p.deathTime > 0);
	public static final NPCDataType<Integer> DEATH_TIME = addInt("death_time", 0, p -> p.deathTime);
	public static final NPCDataType<Float> WALK_ANIMATION_POS = addFloat("walk_animation_pos", 0F, (p, d) -> p.walkAnimation.position(d));
	public static final NPCDataType<Float> WALK_ANIMATION_SPEED = addFloat("walk_animation_speed", 0F, (p, d) -> p.walkAnimation.speed(d));
	public static final NPCDataType<Clothing> CLOTHING = add("clothing", Clothing.STREAM_CODEC, Clothing.NONE, SimilarityCheck.getDefault(), (p, d) -> ClientGameEngine.INSTANCE.getClothing(p));
	public static final NPCDataType<IconHolder> PLUMBOB = add("plumbob", IconHolder.STREAM_CODEC, IconHolder.EMPTY, SimilarityCheck.getDefault(), (p, d) -> ClientGameEngine.INSTANCE.getPlumbob(p));

	@Override
	public String getSerializedName() {
		return name;
	}

	public void write(RegistryFriendlyByteBuf buf, T value) {
		streamCodec.encode(Cast.to(buf), value);
	}

	public T read(RegistryFriendlyByteBuf buf) {
		return streamCodec.decode(Cast.to(buf));
	}

	public boolean isSimilar(T a, T b) {
		return Objects.equals(a, b);
	}
}
