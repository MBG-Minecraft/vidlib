package dev.latvian.mods.vidlib.core;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import dev.latvian.mods.klib.core.KLibExtensions;
import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.api.VLObject;
import dev.latvian.mods.vidlib.feature.entity.VLVehicle;
import dev.latvian.mods.vidlib.feature.misc.CreateFireworksPayload;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class VLExtensions {
	public static MethodHandle getMethodHandle;
	public static MethodHandle setMethodHandle;

	private static Class<?> findClass(String name) throws ClassNotFoundException {
		var classLoader = KLibExtensions.class.getClassLoader();
		ClassLoader previousClassLoader = null;

		while (classLoader != null && classLoader != previousClassLoader) {
			try {
				return Class.forName(name, true, classLoader);
			} catch (Throwable ignored) {
				previousClassLoader = classLoader;
				classLoader = classLoader.getParent();
			}
		}

		throw new ClassNotFoundException(name);
	}

	public static void init() {
		try {
			var lookup = MethodHandles.lookup();

			var c = findClass("dev.latvian.mods.vidlib.core.api.VLObject");
			getMethodHandle = lookup.findVirtual(c, "vl$get", MethodType.methodType(Object.class, int.class));
			setMethodHandle = lookup.findVirtual(c, "vl$set", MethodType.methodType(void.class, int.class, Object.class));

			var b = findClass("dev.latvian.mods.vidlib.core.api.VLBridge");
			lookup.findStaticVarHandle(b, "gameDirectory", Supplier.class).set((Supplier<Path>) () -> PlatformHelper.INSTANCE.getGameDirectory());
			lookup.findStaticVarHandle(b, "disableSleepStatusAnnouncement", Predicate.class).set((Predicate<ServerLevel>) level -> CommonGameEngine.INSTANCE.disableSleepStatusAnnouncement(level));
			lookup.findStaticVarHandle(b, "tickPrecipitation", Function4.class).set((Function4<ServerLevel, BlockPos, BlockPos, BlockPos, Void>) (level, blockPos, motionBlockedPos, belowMotionBlockedPos) -> {
				CommonGameEngine.INSTANCE.tickPrecipitation(level, blockPos, motionBlockedPos, belowMotionBlockedPos);
				return null;
			});
			lookup.findStaticVarHandle(b, "allowFlight", Predicate.class).set((Predicate<Player>) player -> CommonGameEngine.INSTANCE.allowFlight(player));
			lookup.findStaticVarHandle(b, "overrideFluidState", BiFunction.class).set((BiFunction<Level, BlockPos, FluidState>) (level, blockPos) -> CommonGameEngine.INSTANCE.overrideFluidState(level, blockPos));
			lookup.findStaticVarHandle(b, "overrideFluidStateBlock", BiFunction.class).set((BiFunction<Level, BlockPos, BlockState>) (level, blockPos) -> CommonGameEngine.INSTANCE.overrideFluidStateBlock(level, blockPos));
			lookup.findStaticVarHandle(b, "overrideFluidHeight", Function4.class).set((Function4<Level, FluidState, BlockPos, Float, Float>) (level, state, pos, original) -> CommonGameEngine.INSTANCE.overrideFluidHeight(level, state, pos, original));
			lookup.findStaticVarHandle(b, "overrideBarrierShape", Function4.class).set((Function4<BlockState, BlockGetter, BlockPos, CollisionContext, VoxelShape>) (state, blockGetter, pos, ctx) -> CommonGameEngine.INSTANCE.overrideBarrierShape(state, blockGetter, pos, ctx));
			lookup.findStaticVarHandle(b, "clip", Function3.class).set((Function3<BlockGetter, BlockHitResult, ClipContext, BlockHitResult>) (blockGetter, hit, ctx) -> CommonGameEngine.INSTANCE.clip(blockGetter, hit, ctx));
			lookup.findStaticVarHandle(b, "disableGolems", BiPredicate.class).set((BiPredicate<Level, BlockPos>) (level, pos) -> CommonGameEngine.INSTANCE.disableGolems(level, pos));
			lookup.findStaticVarHandle(b, "capture", Function4.class).set((Function4<MinecraftServer, UUID, Packet<?>, Boolean, Void>) (server, player, packet, config) -> {
				var session = ServerSessionData.of(server);

				if (session.isReplay) {
					return null;
				}

				var packetCapture = session.startPacketCapture();

				if (packetCapture != null) {
					packetCapture.getSession(player).capture(packet, config);
				}

				return null;
			});
			lookup.findStaticVarHandle(b, "tickCoralBlocks", Function3.class).set((Function3<ScheduledTickAccess, BlockPos, Block, Boolean>) (tickAccess, pos, block) -> CommonGameEngine.INSTANCE.tickCoralBlocks(tickAccess, pos, block));
			lookup.findStaticVarHandle(b, "getAmbientLight", Float2FloatFunction.class).set((Float2FloatFunction) fallback -> CommonGameEngine.INSTANCE.getAmbientLight(fallback));
			lookup.findStaticVarHandle(b, "allowEmoteCommand", Predicate.class).set((Predicate<CommandSourceStack>) src -> CommonGameEngine.INSTANCE.allowEmoteCommand(src));
			lookup.findStaticVarHandle(b, "allowPlayerListCommand", Predicate.class).set((Predicate<CommandSourceStack>) src -> CommonGameEngine.INSTANCE.allowPlayerListCommand(src));
			lookup.findStaticVarHandle(b, "allowMsgCommand", Predicate.class).set((Predicate<CommandSourceStack>) src -> CommonGameEngine.INSTANCE.allowMsgCommand(src));
			lookup.findStaticVarHandle(b, "allowTeamMsgCommand", Predicate.class).set((Predicate<CommandSourceStack>) src -> CommonGameEngine.INSTANCE.allowTeamMsgCommand(src));
			lookup.findStaticVarHandle(b, "disableXP", Predicate.class).set((Predicate<ServerLevel>) level -> CommonGameEngine.INSTANCE.disableXP(level));
			lookup.findStaticVarHandle(b, "itemInventoryTick", Function3.class).set((Function3<ItemStack, Entity, @Nullable EquipmentSlot, ItemStack>) (stack, entity, slot) -> CommonGameEngine.INSTANCE.itemInventoryTick(stack, entity, slot));
			lookup.findStaticVarHandle(b, "modifyDroppedItem", BiConsumer.class).set((BiConsumer<LivingEntity, ItemEntity>) (parent, item) -> CommonGameEngine.INSTANCE.modifyDroppedItem(parent, item));
			lookup.findStaticVarHandle(b, "disableBlockGravity", Function3.class).set((Function3<Level, BlockPos, BlockState, Boolean>) (level, pos, state) -> CommonGameEngine.INSTANCE.disableBlockGravity(level, pos, state));
			lookup.findStaticVarHandle(b, "replaceFoodTick", BiPredicate.class).set((BiPredicate<ServerPlayer, FoodData>) (player, foodData) -> CommonGameEngine.INSTANCE.replaceFoodTick(player, foodData));
			lookup.findStaticVarHandle(b, "disableIceMelting", Function3.class).set((Function3<Level, BlockPos, BlockState, Boolean>) (level, pos, state) -> CommonGameEngine.INSTANCE.disableIceMelting(level, pos, state));
			lookup.findStaticVarHandle(b, "disableLeafDecay", Predicate.class).set((Predicate<BlockState>) state -> CommonGameEngine.INSTANCE.disableLeafDecay(state));
			lookup.findStaticVarHandle(b, "createFireworks", Function4.class).set((Function4<ServerLevel, Vec3, Vec3, List<FireworkExplosion>, Void>) (level, pos, delta, explosions) -> {
				PlayerTarget.of(level).s2c(new CreateFireworksPayload(pos.x, pos.y, pos.z, delta.x, delta.y, delta.z, explosions));
				return null;
			});
			lookup.findStaticVarHandle(b, "getAttackBoundingBox", BiFunction.class).set((BiFunction<Mob, AABB, AABB>) (mob, box) -> CommonGameEngine.INSTANCE.getAttackBoundingBox(mob, box));
			lookup.findStaticVarHandle(b, "isAngryByDefault", BiPredicate.class).set((BiPredicate<ServerLevel, NeutralMob>) (level, mob) -> CommonGameEngine.INSTANCE.isAngryByDefault(level, mob));
			lookup.findStaticVarHandle(b, "getInfiniteArrows", Supplier.class).set((Supplier<ItemStack>) () -> CommonGameEngine.INSTANCE.getInfiniteArrows());
			lookup.findStaticVarHandle(b, "getArrowDespawnTime", ToIntFunction.class).set((ToIntFunction<AbstractArrow>) arrow -> CommonGameEngine.INSTANCE.getArrowDespawnTime(arrow));
			lookup.findStaticVarHandle(b, "getArrowTrail", Predicate.class).set((Predicate<AbstractArrow>) arrow -> CommonGameEngine.INSTANCE.getArrowTrail(arrow));
			lookup.findStaticVarHandle(b, "spawnArrowTrail", Function3.class).set((Function3<AbstractArrow, Vec3, Vec3, Void>) (arrow, prevPos, pos) -> {
				CommonGameEngine.INSTANCE.spawnArrowTrail(arrow, prevPos, pos);
				return null;
			});
			lookup.findStaticVarHandle(b, "initialPlayerSync", BiConsumer.class).set((BiConsumer<ServerPlayer, Consumer<Packet<? super ClientGamePacketListener>>>) (player, packets) -> CommonGameEngine.INSTANCE.initialPlayerSync(player, packets));
			lookup.findStaticVarHandle(b, "playerRemoved", BiConsumer.class).set((BiConsumer<MinecraftServer, ServerPlayer>) (server, player) -> {
				var packetCapture = ServerSessionData.of(server).packetCapture;

				if (packetCapture != null) {
					packetCapture.disconnect(player.getUUID());
				}
			});
			lookup.findStaticVarHandle(b, "isSmallTallGrassHitbox", Predicate.class).set((Predicate<BlockGetter>) level -> CommonGameEngine.INSTANCE.isSmallTallGrassHitbox(level));
			lookup.findStaticVarHandle(b, "disablePricklyBerryBushes", Function4.class).set((Function4<Level, BlockPos, BlockState, Entity, Boolean>) (level, pos, state, entity) -> CommonGameEngine.INSTANCE.disablePricklyBerryBushes(level, pos, state, entity));
			lookup.findStaticVarHandle(b, "intersectsSolid", Function3.class).set((Function3<Level, @Nullable Entity, AABB, Boolean>) (level, entity, collisionBox) -> LevelSessionData.of(level).intersectsSolid(entity, collisionBox));
			lookup.findStaticVarHandle(b, "collectShapesIntersecting", Function4.class).set((Function4<CollisionGetter, @Nullable Entity, AABB, List<VoxelShape>, Void>) (level, entity, collisionBox, list) -> {
				CommonGameEngine.INSTANCE.collectShapesIntersecting(level, entity, collisionBox, list);
				return null;
			});
			lookup.findStaticVarHandle(b, "getGravityModifier", ToDoubleFunction.class).set((ToDoubleFunction<Entity>) entity -> CommonGameEngine.INSTANCE.getGravityModifier(entity));
			lookup.findStaticVarHandle(b, "sortPassengers", BiConsumer.class).set((BiConsumer<Entity, List<Entity>>) (entity, list) -> {
				if (entity instanceof VLVehicle vehicle) {
					vehicle.sortPassengers(list);
				}
			});
			lookup.findStaticVarHandle(b, "livingTick", Consumer.class).set((Consumer<LivingEntity>) entity -> CommonGameEngine.INSTANCE.livingTick(entity));
			lookup.findStaticVarHandle(b, "getSpeedModifier", ToFloatFunction.class).set((ToFloatFunction<LivingEntity>) entity -> CommonGameEngine.INSTANCE.getSpeedModifier(entity));
			lookup.findStaticVarHandle(b, "isTrackedByLocatorBar", Predicate.class).set((Predicate<LivingEntity>) entity -> CommonGameEngine.INSTANCE.isTrackedByLocatorBar(entity));
			lookup.findStaticVarHandle(b, "isUnpushable", Predicate.class).set((Predicate<LivingEntity>) entity -> CommonGameEngine.INSTANCE.isUnpushable(entity));
			lookup.findStaticVarHandle(b, "calculateFallDamage", Function5.class).set((Function5<LivingEntity, Double, Float, DamageSource, Integer, Integer>) (entity, fallDistance, damageMultiplier, damageSource, original) -> CommonGameEngine.INSTANCE.calculateFallDamage(entity, fallDistance, damageMultiplier, damageSource, original));

			VidLib.LOGGER.info("VLExtensions initialized");
		} catch (Throwable ex) {
			throw new RuntimeException("Couldn't load VLExtensions", ex);
		}
	}

	public static Object getRaw(Object object, int id) {
		if (object instanceof VLObject ext) {
			return ext.vl$get(id);
		}

		try {
			return getMethodHandle.invoke(object, id);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke VLObject#klib$get", ex);
		}
	}

	public static <T> T get(Object object, int id) {
		return (T) getRaw(object, id);
	}

	public static void set(Object object, int id, Object value) {
		if (object instanceof VLObject ext) {
			ext.vl$set(id, value);
			return;
		}

		try {
			setMethodHandle.invoke(object, id, value);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke VLObject#klib$set", ex);
		}
	}

	public static <K, V> V compute(K object, int id, Function<K, V> factory) {
		var value = getRaw(object, id);

		if (value == null) {
			value = factory.apply(object);
			set(object, id, value);
		}

		return (V) value;
	}
}
