package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PoiSection.class)
public class PoiSectionMixin {
	/**
	 * @author Lat
	 * @reason Errors, Optimization
	 */
	@Overwrite
	private boolean add(PoiRecord record) {
		return false;
	}

	/**
	 * @author Lat
	 * @reason Errors, Optimization
	 */
	@Overwrite
	public void add(BlockPos pos, Holder<PoiType> type) {
	}

	/**
	 * @author Lat
	 * @reason Errors, Optimization
	 */
	@Overwrite
	public void remove(BlockPos pos) {
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public boolean exists(BlockPos pos, Predicate<Holder<PoiType>> typePredicate) {
		return false;
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> typePredicate, PoiManager.Occupancy status) {
		return Stream.empty();
	}
}
