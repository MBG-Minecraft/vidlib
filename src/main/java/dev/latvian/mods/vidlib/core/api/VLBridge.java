package dev.latvian.mods.vidlib.core.api;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
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

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class VLBridge {
	public static Predicate<ServerLevel> disableSleepStatusAnnouncement;
	public static Function4<ServerLevel, BlockPos, BlockPos, BlockPos, Void> tickPrecipitation;
	public static Predicate<Player> allowFlight;
	public static BiFunction<Level, BlockPos, FluidState> overrideFluidState;
	public static BiFunction<Level, BlockPos, BlockState> overrideFluidStateBlock;
	public static Function4<Level, FluidState, BlockPos, Float, Float> overrideFluidHeight;
	public static Function4<BlockState, BlockGetter, BlockPos, CollisionContext, VoxelShape> overrideBarrierShape;
	public static Function3<BlockGetter, BlockHitResult, ClipContext, BlockHitResult> clip;
	public static BiPredicate<Level, BlockPos> disableGolems;
	public static Function4<MinecraftServer, UUID, Packet<?>, Boolean, Void> capture;
	public static Function3<ScheduledTickAccess, BlockPos, Block, Boolean> tickCoralBlocks;
	public static Float2FloatFunction getAmbientLight;
	public static Predicate<CommandSourceStack> allowEmoteCommand;
	public static Predicate<ServerLevel> disableXP;
	public static Function3<ItemStack, Entity, @Nullable EquipmentSlot, ItemStack> itemInventoryTick;
	public static BiConsumer<LivingEntity, ItemEntity> modifyDroppedItem;
	public static Function3<Level, BlockPos, BlockState, Boolean> disableBlockGravity;
}
