package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.misc.ClientModInfo;
import dev.latvian.mods.vidlib.feature.net.Context;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
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
	public static final long START_TIME = System.currentTimeMillis();

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

	public boolean isLadder(LevelReader level, BlockPos pos, BlockState state, LivingEntity entity) {
		return false;
	}
}
