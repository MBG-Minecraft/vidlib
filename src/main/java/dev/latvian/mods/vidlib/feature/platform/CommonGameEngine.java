package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.entity.ExactEntitySpawnPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.misc.ClientModInfo;
import dev.latvian.mods.vidlib.feature.net.Context;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class CommonGameEngine {
	public static CommonGameEngine INSTANCE = new CommonGameEngine();

	public void collectServerFeatures(Reference2IntMap<Feature> map) {
		map.put(Feature.INFINITE_CHUNK_RENDERING, 1);
		map.put(Feature.SMALL_GRASS_HITBOX, 1);
		map.put(Feature.SOFT_BARRIERS, 1);
		map.put(Feature.SERVER_DATA, 1);
		map.put(Feature.PLAYER_DATA, 1);
		map.put(Feature.SKYBOX, 1);
	}

	public void setupServer(MinecraftServer server) {
		server.overworld().setDayTime(6000L);
		server.overworld().setWeatherParameters(20000000, 20000000, false, false);
		server.setFlightAllowed(true);

		if (server.isSingleplayer()) {
			server.getPlayerList().setAllowCommandsForAllPlayers(true);
		}

		setupGameRules(server.getGameRules(), server);
	}

	public void setupGameRules(GameRules rules, MinecraftServer server) {
		rules.getRule(GameRules.RULE_DOFIRETICK).set(false, server);
		// rules.getRule(GameRules.RULE_DOBLOCKDROPS).set(false, server);
		rules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(false, server);
		rules.getRule(GameRules.RULE_SPAWN_RADIUS).set(0, server);
		rules.getRule(GameRules.RULE_DISABLE_PLAYER_MOVEMENT_CHECK).set(true, server);
		rules.getRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK).set(true, server);
		rules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, server);
		rules.getRule(GameRules.RULE_DISABLE_RAIDS).set(true, server);
		rules.getRule(GameRules.RULE_DOINSOMNIA).set(false, server);
		rules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_DO_WARDEN_SPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_GLOBAL_SOUND_EVENTS).set(false, server);
	}

	public boolean isPlayerStaff(Collection<String> tags, GameType gameMode) {
		return gameMode == GameType.SPECTATOR || tags.contains("staff");
	}

	public boolean tickCoralBlocks(ScheduledTickAccess instance, BlockPos pos, Block block) {
		return true;
	}

	@Nullable
	public UUID createOfflinePlayerUUID(String name) {
		if (!FMLLoader.isProduction() && !name.startsWith("Player") && !name.startsWith("Dev")) {
			try {
				VidLib.LOGGER.info("Fetching offline UUID for " + name + "...");
				var profile = PlayerProfiles.get(name);

				if (!profile.isError()) {
					VidLib.LOGGER.info("UUID for " + name + " found: " + profile.profile().getId());
					return profile.profile().getId();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}

	public boolean getInfiniteArrows() {
		return false;
	}

	public boolean getArrowTrail(AbstractArrow arrow) {
		return true;
	}

	public int getArrowDespawnTime(AbstractArrow arrow) {
		return 1200;
	}

	public void handlePacket(Context ctx) {
		if (ctx.listener().flow() == PacketFlow.CLIENTBOUND) {
			if (!forwardHandlePacketToClient(ctx)) {
				return;
			}
		}

		ctx.payload().wrapped().handleAsync(ctx);
	}

	private boolean forwardHandlePacketToClient(Context ctx) {
		return ClientGameEngine.INSTANCE.handleClientPacket(ctx);
	}

	public boolean handleClientModList(Context ctx, Map<String, ClientModInfo> mods) {
		/*
		if (player.server.isDedicatedServer()) {
			VidLib.LOGGER.info("Player " + player.getScoreboardName() + " logged in with mods:");

			for (var info : modList) {
				VidLib.LOGGER.info(" - " + info.name() + " (" + info.name() + " / " + info.fileName() + "), " + info.version());
			}
		}
		 */

		return true;
	}

	@Nullable
	public VoxelShape overrideBarrierShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		return null;
	}

	public boolean isSmallTallGrassHitbox(BlockGetter blockGetter) {
		return blockGetter instanceof Level level && level.getServerFeatures().has(Feature.SMALL_GRASS_HITBOX);
	}

	public boolean disablePOI() {
		return false;
	}

	public boolean disableAdvancements() {
		return false;
	}

	public boolean isLadder(LevelReader level, BlockPos pos, BlockState state, LivingEntity entity) {
		return false;
	}

	public boolean disableIceMelting(Level level, BlockPos pos, BlockState state) {
		return false;
	}

	public boolean disableBlockGravity(Level level, BlockPos pos, BlockState state) {
		return false;
	}

	public Iterable<RecipeHolder<?>> modifyRecipeList(Iterable<RecipeHolder<?>> original) {
		return original;
	}

	public int calculateFallDamage(LivingEntity livingEntity, double fallDistance, float damageMultiplier, DamageSource damageSource, int original) {
		if (livingEntity instanceof Player && nonLethalFalling()) {
			int hp = Mth.floor(livingEntity.getHealth());

			if (original >= hp) {
				return hp - 1;
			}
		}

		return original;
	}

	public boolean nonLethalFalling() {
		return false;
	}

	public boolean replaceFoodTick(ServerPlayer player, FoodData foodData) {
		return false;
	}

	public float getBlockDensity(BlockState state) {
		var b = state.getBlock();

		if (state.isAir() || b instanceof LightBlock || b instanceof BarrierBlock || b instanceof FireBlock) {
			return 0F;
		} else if (b instanceof CarpetBlock || b instanceof ButtonBlock || b instanceof PressurePlateBlock || b instanceof VineBlock || b instanceof LadderBlock) {
			return 0.06125F;
		} else if (b instanceof DoorBlock || b instanceof SnowLayerBlock || b instanceof FlowerPotBlock) {
			return 0.125F;
		} else if (b instanceof SlabBlock || b instanceof CrossCollisionBlock || b instanceof FenceGateBlock || b instanceof EnchantingTableBlock) {
			return 0.5F;
		} else if (b instanceof VegetationBlock) {
			return 0.25F;
		} else if (b instanceof SimpleWaterloggedBlock || b instanceof HopperBlock) {
			return 0.75F;
		} else {
			return 1F;
		}
	}

	@Nullable
	public Packet<ClientGamePacketListener> overrideEntitySpawnPacket(Entity entity, ServerEntity serverEntity) {
		if (!entity.getType().builtInRegistryHolder().getKey().location().getNamespace().equals("minecraft")) {
			return (Packet) new ExactEntitySpawnPayload(entity, serverEntity, 0).toGameS2C(entity.level());
		}

		return null;
	}
}
