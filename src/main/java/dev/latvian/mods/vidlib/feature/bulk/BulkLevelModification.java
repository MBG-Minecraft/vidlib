package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.BasicRegistryRef;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.structure.LazyStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.Set;

public interface BulkLevelModification extends SimpleRegistryEntry {
	SimpleRegistry<BulkLevelModification> REGISTRY = SimpleRegistry.create(VidLib.id("bulk_level_modification"), c -> PlatformHelper.CURRENT.collectBulkLevelModifications(c));

	BulkLevelModification NONE = new BulkLevelModificationBundle(List.of());

	static void builtinTypes(SimpleRegistryCollector<BulkLevelModification> registry) {
		registry.register(BulkLevelModificationBundle.TYPE);
		registry.register(PositionedBlock.TYPE);
		registry.register(ReplaceCuboidBlocks.TYPE);
		registry.register(ReplaceSphereBlocks.TYPE);
		registry.register(ReplaceSectionBlocks.TYPE);
		registry.register(ReplaceAllSectionBlocks.TYPE);
	}

	static BulkLevelModification allOf(List<BulkLevelModification> list) {
		return list.isEmpty() ? NONE : list.size() == 1 ? list.getFirst() : new BulkLevelModificationBundle(list);
	}

	static BulkLevelModification structure(
		BasicRegistryRef<ResourceLocation, LazyStructures> templateRef,
		BlockPos pos,
		BlockPos offset,
		Mirror mirror,
		Rotation rotation,
		BlockPos rotationPivot,
		long randomSeed
	) {
		try {
			var template = templateRef.get().get();

			if (template != null) {
				var palette = template.size() == 1 ? template.getFirst() : template.get(RandomSource.create(randomSeed == 0L ? Mth.getSeed(pos) : randomSeed).nextInt(template.size()));
				return palette.createModification(pos, offset, mirror, rotation, rotationPivot);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return NONE;
	}

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	void collectSections(Level level, Set<SectionPos> sections);

	void apply(BlockModificationConsumer blocks);

	default BulkLevelModification optimize() {
		return this;
	}
}
