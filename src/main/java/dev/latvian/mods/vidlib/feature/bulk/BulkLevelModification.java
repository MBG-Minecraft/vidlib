package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.BasicRegistryRef;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
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

public interface BulkLevelModification {
	SimpleRegistry<BulkLevelModification> REGISTRY = SimpleRegistry.create(BulkLevelModification::type);
	BulkLevelModification NONE = new BulkLevelModificationBundle(List.of());

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(BulkLevelModificationBundle.TYPE);
		REGISTRY.register(PositionedBlock.TYPE);
		REGISTRY.register(ReplaceCuboidBlocks.TYPE);
		REGISTRY.register(ReplaceSphereBlocks.TYPE);
		REGISTRY.register(ReplaceSectionBlocks.TYPE);
		REGISTRY.register(ReplaceAllSectionBlocks.TYPE);
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

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	void collectSections(Level level, Set<SectionPos> sections);

	void apply(BlockModificationConsumer blocks);

	default BulkLevelModification optimize() {
		return this;
	}
}
