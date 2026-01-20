package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLChunkMap;
import dev.latvian.mods.vidlib.core.VLServerLevel;
import dev.latvian.mods.vidlib.core.VLServerPacketListener;
import dev.latvian.mods.vidlib.feature.misc.CreateFireworksPayload;
import dev.latvian.mods.vidlib.feature.prop.ServerProps;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements VLServerLevel {
	@Shadow
	@Final
	@Mutable
	private ServerChunkCache chunkSource;

	@Shadow
	@Final
	private MinecraftServer server;

	@Unique
	private ActiveZones vl$activeZones;

	@Unique
	private final LongSet vl$anchoredChunks = new LongOpenHashSet();

	@Unique
	private ServerProps vl$props;

	@Unique
	private Boolean vl$isReplayLevel;

	protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Override
	@Nullable
	public ActiveZones vl$getActiveZones() {
		return vl$activeZones;
	}

	@Override
	public void vl$setActiveZones(ActiveZones zones) {
		if (vl$activeZones != zones) {
			vl$activeZones = zones;
			vl$updateLoadedChunks();
		}
	}

	@Override
	public void vl$updateLoadedChunks() {
		vl$updateLoadedChunks(vl$anchoredChunks);
	}

	@Override
	public ServerProps getProps() {
		if (vl$props == null) {
			vl$props = new ServerProps(vl$level());
		}

		return vl$props;
	}

	@Override
	public void createFireworks(double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, List<FireworkExplosion> explosions) {
		s2c(new CreateFireworksPayload(x, y, z, xSpeed, ySpeed, zSpeed, explosions));
	}

	@Override
	public void vl$reloadChunks() {
		((VLChunkMap) chunkSource.chunkMap).vl$reloadChunks();
	}

	@Override
	public boolean isReplayLevel() {
		if (vl$isReplayLevel == null) {
			vl$isReplayLevel = vl$level().getServer().getClass().getName().equals("com.moulberry.flashback.playback.ReplayServer");
		}

		return vl$isReplayLevel;
	}

	// Other mods like Motion Capture mod create fake players, so this avoid a null pointer.
	@Inject(method = "addPlayer", at = @At("HEAD"))
	private void vl$addPlayer(ServerPlayer player, CallbackInfo ci) {
		if (player.vl$sessionData() != null) {
			return;
		}

		var newPlayerSession = new ServerSessionData(server, player.getUUID());
		((VLServerPacketListener) player.connection).vl$sessionData(newPlayerSession);
		newPlayerSession.load(server);
	}
}
