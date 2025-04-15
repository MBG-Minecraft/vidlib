package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerChunkMap;
import dev.beast.mods.shimmer.core.ShimmerServerLevel;
import dev.beast.mods.shimmer.feature.misc.CreateFireworksPayload;
import dev.beast.mods.shimmer.feature.prop.ServerPropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
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

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ShimmerServerLevel {
	@Shadow
	@Final
	@Mutable
	private ServerChunkCache chunkSource;

	@Unique
	private ActiveZones shimmer$activeZones;

	@Unique
	private final LongSet shimmer$anchoredChunks = new LongOpenHashSet();

	@Unique
	private ServerPropList shimmer$props;

	protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Override
	@Nullable
	public ActiveZones shimmer$getActiveZones() {
		return shimmer$activeZones;
	}

	@Override
	public void shimmer$setActiveZones(ActiveZones zones) {
		if (shimmer$activeZones != zones) {
			shimmer$activeZones = zones;
			shimmer$updateLoadedChunks();
		}
	}

	@Override
	public void shimmer$updateLoadedChunks() {
		shimmer$updateLoadedChunks(shimmer$anchoredChunks);
	}

	@Override
	public ServerPropList getProps() {
		if (shimmer$props == null) {
			shimmer$props = new ServerPropList(this.shimmer$level());
		}

		return shimmer$props;
	}

	@Override
	public void createFireworks(double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, List<FireworkExplosion> explosions) {
		s2c(new CreateFireworksPayload(x, y, z, xSpeed, ySpeed, zSpeed, explosions));
	}

	@Override
	public void shimmer$reloadChunks() {
		((ShimmerChunkMap) chunkSource.chunkMap).shimmer$reloadChunks();
	}
}
