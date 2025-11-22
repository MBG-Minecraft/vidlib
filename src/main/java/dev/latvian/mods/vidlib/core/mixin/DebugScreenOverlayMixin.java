package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Mixin(value = DebugScreenOverlay.class, priority = 1337) // Ensure this mixin runs after others
public abstract class DebugScreenOverlayMixin {

	@Shadow
	private ChunkPos lastPos;

	@Final
	@Shadow
	private Minecraft minecraft;

	@Shadow
	@Nullable
	private LevelChunk clientChunk;

	@Shadow
	public void clearChunkCache() {
	}

	@Shadow
	private LevelChunk getClientChunk() {
		return null;
	}

	@Shadow
	private static String printBiome(Holder<Biome> biomeHolder) {
		return null;
	}

	@Inject(method = "getSystemInformation", at = @At("RETURN"), cancellable = true)
	private void getSystemInformation(CallbackInfoReturnable<List<String>> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue(new ArrayList<>());
		}
	}

	@Inject(method = "showDebugScreen", at = @At("RETURN"), cancellable = true)
	private void showDebugScreen(CallbackInfoReturnable<Boolean> cir) {
		if (Boolean.TRUE.equals(EntityOverride.DISABLE_DEBUG.get(minecraft.player))) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "collectGameInformationText", at = @At("RETURN"), cancellable = true)
	private void collectGameInformationText(CallbackInfoReturnable<List<String>> cir) {
		if (minecraft.showOnlyReducedInfo()) {
			List<String> list = new ArrayList<>();
			list.add("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")");
			list.add(minecraft.fpsString);

			BlockPos blockpos = minecraft.getCameraEntity().blockPosition();
			Entity entity = minecraft.getCameraEntity();
			Direction direction = entity.getDirection();
			ChunkPos chunkpos = new ChunkPos(blockpos);
			if (!Objects.equals(this.lastPos, chunkpos)) {
				this.lastPos = chunkpos;
				this.clearChunkCache();
			}

			String s;
			switch (direction) {
				case NORTH -> s = "Towards negative Z";
				case SOUTH -> s = "Towards positive Z";
				case WEST -> s = "Towards negative X";
				case EAST -> s = "Towards positive X";
				default -> s = "Invalid";
			}

			list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, s, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot())));
			list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", minecraft.getCameraEntity().getX(), minecraft.getCameraEntity().getY(), minecraft.getCameraEntity().getZ()));
			list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkpos.x, SectionPos.blockToSectionCoord(blockpos.getY()), chunkpos.z, chunkpos.getRegionLocalX(), chunkpos.getRegionLocalZ(), chunkpos.getRegionX(), chunkpos.getRegionZ()));
			list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));

			LevelChunk levelchunk = getClientChunk();
			if (levelchunk == null || levelchunk.isEmpty()) {
				list.add("Waiting for chunk...");
			} else {
				int i = minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
				int j = minecraft.level.getBrightness(LightLayer.SKY, blockpos);
				int k = minecraft.level.getBrightness(LightLayer.BLOCK, blockpos);
				list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
				if (minecraft.level.isInsideBuildHeight(blockpos.getY())) {
					Holder<Biome> biome = minecraft.level.getBiome(blockpos);
					list.add("Biome: " + printBiome(biome));
				}
			}

			list.add(minecraft.levelRenderer.getEntityStatistics().replace("E:", "Entities:"));
			cir.setReturnValue(list);
		}
	}

	@Inject(method = "showNetworkCharts", at = @At("RETURN"), cancellable = true)
	public void showNetworkCharts(CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "showProfilerChart", at = @At("RETURN"), cancellable = true)
	public void showProfilerChart(CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "showFpsCharts", at = @At("RETURN"), cancellable = true)
	public void showFpsCharts(CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue(false);
		}
	}

}
