package dev.latvian.mods.vidlib.core;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import dev.latvian.mods.klib.core.KLibExtensions;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.api.VLObject;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

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
			lookup.findStaticVarHandle(b, "disableXP", Predicate.class).set((Predicate<ServerLevel>) level -> CommonGameEngine.INSTANCE.disableXP(level));
			lookup.findStaticVarHandle(b, "itemInventoryTick", Function3.class).set((Function3<ItemStack, Entity, @Nullable EquipmentSlot, ItemStack>) (stack, entity, slot) -> CommonGameEngine.INSTANCE.itemInventoryTick(stack, entity, slot));
			lookup.findStaticVarHandle(b, "modifyDroppedItem", BiConsumer.class).set((BiConsumer<LivingEntity, ItemEntity>) (parent, item) -> CommonGameEngine.INSTANCE.modifyDroppedItem(parent, item));
			lookup.findStaticVarHandle(b, "disableBlockGravity", Function3.class).set((Function3<Level, BlockPos, BlockState, Boolean>) (level, pos, state) -> CommonGameEngine.INSTANCE.disableBlockGravity(level, pos, state));

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
