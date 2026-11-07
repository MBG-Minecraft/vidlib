package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.platform.PlatformModInfo;
import dev.latvian.mods.klib.util.RuntimeDebugger;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.mrbeastgaming.mods.hub.api.data.HubFileType;
import dev.mrbeastgaming.mods.hub.api.data.HubUsedPort;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldDirectory;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommonGameEngine {
	public static CommonGameEngine INSTANCE = new CommonGameEngine();

	public boolean privacyMode() {
		return false;
	}

	public void collectServerFeatures(Reference2IntMap<Feature> map) {
		map.put(Feature.INFINITE_CHUNK_RENDERING, 1);
		map.put(Feature.SMALL_GRASS_HITBOX, 1);
		map.put(Feature.SOFT_BARRIERS, 1);
		map.put(Feature.SERVER_DATA, 1);
		map.put(Feature.PLAYER_DATA, 1);
		map.put(Feature.SKYBOX, 1);
	}

	public void setupServer(MinecraftServer server) {
		var overworldClock = server.overworld().dimensionTypeRegistration().value().defaultClock();
		if (overworldClock.isPresent()) {
			server.clockManager().setTotalTicks(overworldClock.get(), 6000);
		}

		server.setWeatherParameters(20000000, 20000000, false, false);
		// 26.3 removed MinecraftServer#setFlightAllowed: flight is now a per-player ability
		// (see the mayFly hook in core/mixin/neo/IPlayerExtensionMixin.java).

		if (server.isSingleplayer()) {
			// 26.3 removed PlayerList#setAllowCommandsForAllPlayers: command permission is now
			// derived from MinecraftServer#getProfilePermissions, which already grants the
			// default permission level to every player.
		}

		setupGameRules(server.getGameRules(), server);
	}

	public void setupGameRules(GameRules rules, MinecraftServer server) {
		rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0, server);
		// rules.set(GameRules.BLOCK_DROPS, false, server);
		rules.set(GameRules.COMMAND_BLOCK_OUTPUT, false, server);
		rules.set(GameRules.RESPAWN_RADIUS, 0, server);
		rules.set(GameRules.PLAYER_MOVEMENT_CHECK, false, server);
		rules.set(GameRules.ELYTRA_MOVEMENT_CHECK, false, server);
		rules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server);
		rules.set(GameRules.RAIDS, false, server);
		rules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 0, server);
		rules.set(GameRules.SPAWN_PATROLS, false, server);
		rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
		rules.set(GameRules.SPAWN_WARDENS, false, server);
		rules.set(GameRules.GLOBAL_SOUND_EVENTS, false, server);

		if (privacyMode()) {
			rules.set(GameRules.SHOW_DEATH_MESSAGES, false, server);
		}
	}

	public boolean isPlayerStaff(Collection<String> tags, @Nullable GameType gameMode) {
		return tags.contains("staff");
	}

	public boolean isPlayerStaffOrTalent(Collection<String> tags, GameType gameMode) {
		return isPlayerStaff(tags, gameMode) || tags.contains("talent");
	}

	public boolean tickCoralBlocks(ScheduledTickAccess instance, BlockPos pos, Block block) {
		return true;
	}

	@Nullable
	public UUID createOfflinePlayerUUID(String name) {
		if (!FMLLoader.getCurrent().isProduction() && !name.startsWith("Player") && !name.startsWith("Dev")) {
			try {
				VidLib.LOGGER.info("Fetching offline UUID for " + name + "...");
				var profile = PlayerProfiles.get(name);

				if (!profile.isError()) {
					VidLib.LOGGER.info("UUID for " + name + " found: " + profile.profile().id());
					return profile.profile().id();
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

		ctx.payload().handleAsync(ctx);
	}

	private boolean forwardHandlePacketToClient(Context ctx) {
		return ClientGameEngine.INSTANCE.handleClientPacket(ctx);
	}

	public boolean handleClientModList(Context ctx, Map<String, PlatformModInfo> mods) {
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
		if (context instanceof EntityCollisionContext ctx && ctx.getEntity() != null && blockGetter instanceof Level level && LevelSessionData.of(level).worldData.getServerFeatures().has(Feature.SOFT_BARRIERS)) {
			if (ctx.getEntity() instanceof AbstractArrow) {
				return Shapes.empty();
			}

			if (EntityUtils.isCreative(ctx.getEntity())) {
				return Shapes.empty();
			}
		}

		return null;
	}

	public boolean isSmallTallGrassHitbox(BlockGetter blockGetter) {
		return blockGetter instanceof Level level && LevelSessionData.of(level).worldData.getServerFeatures().has(Feature.SMALL_GRASS_HITBOX);
	}

	public boolean disablePOI() {
		return false;
	}

	public Collection<AdvancementHolder> overrideAdvancements(Collection<AdvancementHolder> original) {
		return original;
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

	public boolean disableJoinMessages() {
		return true;
	}

	public void heal(LivingEntity entity) {
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(entity.getMaxHealth());
		}

		entity.extinguishFire();

		if (entity instanceof ServerPlayer player) {
			player.getFoodData().eat(20, 20F);
		}
	}

	public boolean disableGolems(Level level, BlockPos pos) {
		return false;
	}

	public boolean disableXP(Level level) {
		return false;
	}

	public boolean disableLeafDecay(BlockState state) {
		return false;
	}

	public boolean disablePricklyBerryBushes(Level level, BlockPos pos, BlockState state, Entity entity) {
		return false;
	}

	public float getAmbientLight(float fallback) {
		if (FMLLoader.getCurrent().getDist().isClient()) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				float value = player.get(InternalPlayerData.BRIGHTNESS_OVERRIDE);

				if (value > 0F) {
					return value;
				}
			}
		}
		return fallback;
	}

	public boolean disablePacketCapture() {
		return true;
	}

	public void livingTick(LivingEntity entity) {
		if (!entity.level().isClientSide() && entity.getHealth() < entity.getMaxHealth()) {
			var regen = getRegenerationTicks(entity);

			if (regen >= 0) {
				if (regen == 0) {
					entity.heal();
				} else if (entity.tickCount % regen == 0) {
					entity.heal(1F);
				}
			}
		}

		if (entity.getGravity() <= 0D) {
			entity.resetFallDistance();
		}

		if (entity.vl$isSuspended()) {
			entity.setDeltaMovement(Vec3.ZERO);
		}
	}

	public ItemStack itemInventoryTick(ItemStack stack, Entity entity, @Nullable EquipmentSlot slot) {
		return stack;
	}

	public int getRegenerationTicks(LivingEntity entity) {
		return -1;
	}

	public boolean isSuspended(Entity entity) {
		return entity instanceof Player player && player.vl$sessionData().suspended;
	}

	public double getGravityModifier(Entity entity) {
		return isSuspended(entity) ? 0D : 1D;
	}

	public float getSpeedModifier(LivingEntity entity) {
		return isSuspended(entity) ? 0F : 1F;
	}

	public float getFlightSpeedModifier(Player player) {
		return player.get(InternalPlayerData.FLIGHT_SPEED);
	}

	public boolean allowPVP(Player from, Player to) {
		return !from.vl$isSuspended() && !to.vl$isSuspended();
	}

	public boolean canVoicechatBroadcast(Player player) {
		return player.isStaff() || player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public boolean isInvulnerable(Entity entity) {
		return false;
	}

	public float getAttackDamage(LivingEntity entity, DamageSource source, float original) {
		return isSuspended(entity) ? 0F : original;
	}

	public boolean isUnpushable(LivingEntity entity) {
		return isSuspended(entity);
	}

	public boolean getScaleDamageWithDifficulty(ServerPlayer player) {
		return true;
	}

	public boolean intersectsSolid(CollisionGetter collisionGetter, @Nullable Entity entity, AABB collisionBox) {
		return collisionGetter instanceof Level level && LevelSessionData.of(level).intersectsSolid(entity, collisionBox);
	}

	public void collectShapesIntersecting(CollisionGetter collisionGetter, @Nullable Entity entity, AABB collisionBox, List<VoxelShape> shapes) {
		if (collisionGetter instanceof Level level) {
			LevelSessionData.of(level).collectShapesIntersecting(entity, collisionBox, shapes);
		}
	}

	public FluidState overrideFluidState(Level level, BlockPos pos) {
		var original = level.getFluidState(pos);

		if (original.isEmpty()) {
			var state = LevelSessionData.of(level).getFluidState(pos);

			if (state != null) {
				return state;
			}
		}

		return original;
	}

	public BlockState overrideFluidStateBlock(Level level, BlockPos pos) {
		return overrideFluidState(level, pos).createLegacyBlock();
	}

	public float overrideFluidHeight(Level level, FluidState state, BlockPos pos, float original) {
		if (state.getType() instanceof FlowingFluid flowing) {
			var height = LevelSessionData.of(level).getFluidHeight(flowing, pos);

			if (height > 0F) {
				return height;
			}
		}

		return original;
	}

	public BlockHitResult clip(BlockGetter blockGetter, BlockHitResult result, ClipContext ctx) {
		if (blockGetter instanceof Level level) {
			return LevelSessionData.of(level).clip(result, ctx);
		}

		return result;
	}

	public void blockChanged(Level level, BlockPos pos, BlockState from, BlockState to) {
		if (level instanceof ServerLevel serverLevel) {
			var fromAnchor = isBlockAnchor(serverLevel, pos, from);
			var toAnchor = isBlockAnchor(serverLevel, pos, to);

			if (fromAnchor != toAnchor) {
				Anchor.setAnchorBlockActive(serverLevel, pos, toAnchor);
			}
		}
	}

	public boolean isBlockAnchor(ServerLevel level, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof CommandBlock;
	}

	public boolean allowEmoteCommand(CommandSourceStack source) {
		return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public boolean allowPlayerListCommand(CommandSourceStack source) {
		return !privacyMode() || source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public boolean allowMsgCommand(CommandSourceStack source) {
		return !privacyMode() || source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public boolean allowTeamMsgCommand(CommandSourceStack source) {
		return !privacyMode() || source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public void modifyDroppedItem(LivingEntity entity, ItemEntity item) {
	}

	public boolean disableSleepStatusAnnouncement(ServerLevel level) {
		return privacyMode();
	}

	public void tickPrecipitation(ServerLevel level, BlockPos blockPos, BlockPos motionBlockedPos, BlockPos belowMotionBlockedPos) {
	}

	public AABB getAttackBoundingBox(Mob mob, AABB original) {
		return original;
	}

	public boolean isAngryByDefault(ServerLevel level, NeutralMob mob) {
		return false;
	}

	public boolean hasImprovedPlayerTags() {
		return false;
	}

	public boolean getReceiveCommandFeedback(MinecraftServer server, ServerPlayer player) {
		return player.isStaff()/* || server.getPlayerList().isOp(player.getGameProfile())*/;
	}

	public String getBackupInfo(MinecraftServer server, Instant now) {
		return StringUtils.FILE_TIMESTAMP_FORMAT.format(now);
	}

	public String getFullBackupInfo(MinecraftServer server, Instant now, String customName) {
		var name = getBackupInfo(server, now);

		if (!customName.isEmpty()) {
			name += "-" + customName.replaceAll("[^\\w-+]", "").replaceAll("-{2,}", "-");
		}

		while (name.startsWith("-")) {
			name = name.substring(1);
		}

		while (name.endsWith("-")) {
			name = name.substring(0, name.length() - 1);
		}

		if (name.isEmpty()) {
			name = StringUtils.FILE_TIMESTAMP_FORMAT.format(now);
		}

		return name;
	}

	public void initialPlayerSync(ServerPlayer player, java.util.function.Consumer<Packet<ClientGamePacketListener>> callback) {
		// callback.accept(new SyncPlayerTagsPayload(player.getUUID(), List.copyOf(player.entityTags())).toS2C(player.level().getGameTime()));
	}

	public void tickPlayerSync(ServerPlayer player) {
		/*
		var tags = player.entityTags();

		if (vl$prevTags == null || !vl$prevTags.equals(tags)) {
			vl$prevTags = Set.copyOf(tags);
			player.getServer().vl$level().s2c(new SyncPlayerTagsPayload(player.getUUID(), List.copyOf(tags)));
		}
		 */
	}

	public boolean allowFlight(Player player) {
		return PlayerSessionData.of(player).dataMap.get(InternalPlayerData.CAN_FLY);
	}

	public String getServerGatewayName(MinecraftServer server) {
		return ChatFormatting.stripFormatting(server.getMotd().replace("\\n", "\n"));
	}

	public String getServerGatewayStatus(MinecraftServer server) {
		return "";
	}

	public void getUsedPorts(MinecraftServer server, List<HubUsedPort> list) {
		if (server.isPublished()) {
			list.add(HubUsedPort.tcp("Game", server.getPort()));
		}

		int port = RuntimeDebugger.getJdwpListenerPort();

		if (port != -1) {
			list.add(HubUsedPort.tcp("JDWP", port));
		}

		if (server instanceof DedicatedServer dedicatedServer) {
			if (dedicatedServer.getProperties().enableRcon) {
				list.add(HubUsedPort.tcp("RCON", dedicatedServer.getProperties().rconPort));
			}

			if (dedicatedServer.getProperties().enableQuery) {
				list.add(HubUsedPort.udp("Query", dedicatedServer.getProperties().queryPort));
			}
		}
	}

	public void getAvailableWorlds(MinecraftServer server, List<HubWorldDirectory> list) {
		try {
			var root = PlatformHelper.INSTANCE.getGameDirectory().toRealPath().toAbsolutePath();
			var path = server.getWorldPath(LevelResource.ROOT).toRealPath();
			list.add(HubWorldDirectory.of(root, server.getWorldData().getLevelName(), path.getFileName().toString(), path));
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to list available world", ex);
		}
	}

	public void collectServerUploads(MinecraftServer server, HubFileUploads uploads) {
		if (server.isDedicatedServer()) {
			var gameDir = uploads.getGameDirectory();
			uploads.addDirectory(gameDir.resolve("crash-reports"), "-server.txt", HubFileType.SERVER_CRASH_REPORT);
			uploads.addDirectory(gameDir.resolve("crash-reports"), "-fml.txt", HubFileType.MOD_LOADER_CRASH_REPORT);
		}
	}
}
