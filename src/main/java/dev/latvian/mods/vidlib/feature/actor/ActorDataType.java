package dev.latvian.mods.vidlib.feature.actor;

import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.SimilarityCheck;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.player.AbstractClientPlayer;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public record ActorDataType<T>(String name, StreamCodec<?, T> streamCodec, T defaultValue, SimilarityCheck<T> similarityCheck, ActorValueGetter<T> getter) implements StringRepresentable {
	public static final Map<String, ActorDataType<?>> MAP = new Object2ObjectOpenHashMap<>();

	public static <T> ActorDataType<T> add(String name, StreamCodec<?, T> streamCodec, T defaultValue, SimilarityCheck<T> similarityCheck, ActorValueGetter<T> getter) {
		var type = new ActorDataType<>(name, streamCodec, defaultValue, similarityCheck, getter);
		MAP.put(name, type);
		return type;
	}

	public static <T> ActorDataType<T> add(String name, StreamCodec<?, T> streamCodec, T defaultValue, Function<Player, T> getter) {
		return add(name, streamCodec, defaultValue, SimilarityCheck.getDefault(), (player, delta) -> getter.apply(player));
	}

	public static ActorDataType<Integer> addInt(String name, int defaultValue, ToIntFunction<Player> getter) {
		return add(name, ByteBufCodecs.VAR_INT, defaultValue, SimilarityCheck.getDefault(), (player, delta) -> getter.applyAsInt(player));
	}

	public static ActorDataType<Float> addFloat(String name, float defaultValue, ActorValueGetter<Float> getter) {
		return add(name, ByteBufCodecs.FLOAT, defaultValue, SimilarityCheck.FLOAT, getter);
	}

	public static ActorDataType<Boolean> addBoolean(String name, boolean defaultValue, Predicate<Player> getter) {
		return add(name, ByteBufCodecs.BOOL, defaultValue, SimilarityCheck.getDefault(), (player, delta) -> getter.test(player));
	}

	public static ActorDataType<ItemStack> addEquipment(String name, EquipmentSlot slot) {
		return add(name, ItemStack.OPTIONAL_STREAM_CODEC, ItemStack.EMPTY, SimilarityCheck.ITEM_STACK, (player, delta) -> player.getItemBySlot(slot).copy());
	}

	public static final ActorDataType<Vec3> POSITION = add("position", MCStreamCodecs.VEC3, Vec3.ZERO, SimilarityCheck.VEC3, Entity::getPosition);
	public static final ActorDataType<Float> YAW = addFloat("yaw", 0F, Entity::getViewYRot);
	public static final ActorDataType<Float> PITCH = addFloat("pitch", 0F, Entity::getViewXRot);
	public static final ActorDataType<Float> BODY_YAW = addFloat("body_yaw", 0F, (p, d) -> Mth.lerp(d, p.yBodyRotO, p.yBodyRot));
	public static final ActorDataType<Float> HEAD_YAW = addFloat("head_yaw", 0F, (p, d) -> Mth.lerp(d, p.yHeadRotO, p.yHeadRot));
	public static final ActorDataType<Vec3> VELOCITY = add("velocity", MCStreamCodecs.VEC3S, Vec3.ZERO, SimilarityCheck.VEC3, (p, d) -> p.getDeltaMovement());
	public static final ActorDataType<ItemStack> MAIN_HAND = addEquipment("equipment/hand/main", EquipmentSlot.MAINHAND);
	public static final ActorDataType<ItemStack> OFF_HAND = addEquipment("equipment/hand/off", EquipmentSlot.OFFHAND);
	public static final ActorDataType<ItemStack> EQUIPMENT_FEET = addEquipment("equipment/feet", EquipmentSlot.FEET);
	public static final ActorDataType<ItemStack> EQUIPMENT_LEGS = addEquipment("equipment/legs", EquipmentSlot.LEGS);
	public static final ActorDataType<ItemStack> EQUIPMENT_CHEST = addEquipment("equipment/chest", EquipmentSlot.CHEST);
	public static final ActorDataType<ItemStack> EQUIPMENT_HEAD = addEquipment("equipment/head", EquipmentSlot.HEAD);
	public static final ActorDataType<Float> HEALTH = addFloat("health", 20F, (p, d) -> p.getHealth());
	public static final ActorDataType<Float> MAX_HEALTH = addFloat("max_health", 20F, (p, d) -> p.getMaxHealth());
	public static final ActorDataType<Pose> POSE = add("pose", Pose.STREAM_CODEC, Pose.STANDING, Entity::getPose);
	public static final ActorDataType<Boolean> SWINGING = addBoolean("swinging", false, LivingEntity::isSwinging);
	public static final ActorDataType<Float> SWIM_AMOUNT = addFloat("swim_amount", 0F, LivingEntity::getSwimAmount);
	public static final ActorDataType<Float> ATTACK_TIME = addFloat("attack_time", 0F, (p, d) -> p.getSwingAnimation(d));
	public static final ActorDataType<Boolean> PASSENGER = addBoolean("passenger", false, p -> p.isPassenger() && (p.getVehicle() != null && p.getVehicle().shouldRiderSit()));
	public static final ActorDataType<Boolean> USING_ITEM = addBoolean("using_item", false, LivingEntity::isUsingItem);
	public static final ActorDataType<Boolean> GLOWING = addBoolean("glowing", false, Entity::isCurrentlyGlowing);
	public static final ActorDataType<Boolean> RED_OVERLAY = addBoolean("red_overlay", false, p -> p.hurtTime > 0 || p.deathTime > 0);
	public static final ActorDataType<Integer> DEATH_TIME = addInt("death_time", 0, p -> p.deathTime);
	public static final ActorDataType<Float> WALK_ANIMATION_POS = addFloat("walk_animation_pos", 0F, (p, d) -> p.walkAnimation.position(d));
	public static final ActorDataType<Float> WALK_ANIMATION_SPEED = addFloat("walk_animation_speed", 0F, (p, d) -> p.walkAnimation.speed(d));
	public static final ActorDataType<PlayerClothing> CLOTHING = add("clothing", PlayerClothing.DATA_TYPE.streamCodec(), PlayerClothing.NONE, (p) -> p instanceof AbstractClientPlayer player ? ClientGameEngine.INSTANCE.getClothing(player) : PlayerClothing.NONE);
	public static final ActorDataType<Ref<Icon>> PLUMBOB = add("plumbob", Icon.STREAM_CODEC, Icon.EMPTY, SimilarityCheck.getDefault(), (p, d) -> ClientGameEngine.INSTANCE.getPlumbob(p));

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
