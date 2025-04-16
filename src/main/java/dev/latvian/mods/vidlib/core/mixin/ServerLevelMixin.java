package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLChunkMap;
import dev.latvian.mods.vidlib.core.VLServerLevel;
import dev.latvian.mods.vidlib.feature.misc.CreateFireworksPayload;
import dev.latvian.mods.vidlib.feature.prop.ServerPropList;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
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
public abstract class ServerLevelMixin extends Level implements VLServerLevel {
	@Shadow
	@Final
	@Mutable
	private ServerChunkCache chunkSource;

	@Unique
	private ActiveZones vl$activeZones;

	@Unique
	private final LongSet vl$anchoredChunks = new LongOpenHashSet();

	@Unique
	private ServerPropList vl$props;

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
	public ServerPropList getProps() {
		if (vl$props == null) {
			vl$props = new ServerPropList(this.vl$level());
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
}
