package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.registry.BasicRegistryRef;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
		BasicRegistryRef<ResourceLocation, Lazy<StructureTemplate>> templateRef,
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
				return structure(
					template,
					pos,
					offset,
					mirror,
					rotation,
					rotationPivot,
					randomSeed
				);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return NONE;
	}

	static BulkLevelModification structure(
		StructureTemplate template,
		BlockPos pos,
		BlockPos offset,
		Mirror mirror,
		Rotation rotation,
		BlockPos rotationPivot,
		long randomSeed
	) {
		if (template == null || template.palettes.isEmpty() || template.getSize().getX() < 1 || template.getSize().getY() < 1 || template.getSize().getZ() < 1) {
			return NONE;
		}

		var random = RandomSource.create(randomSeed == 0L ? Mth.getSeed(pos) : randomSeed);
		var palette = template.palettes.size() == 1 ? template.palettes.getFirst() : template.palettes.get(random.nextInt(template.palettes.size()));
		var list = palette.blocks();
		var builder = new OptimizedModificationBuilder();

		for (var info : list) {
			if (info.state().is(Blocks.STRUCTURE_VOID)) {
				continue;
			}

			var blockPos = StructureTemplate.transform(info.pos().offset(offset), mirror, rotation, rotationPivot).offset(pos);
			var state = info.state().mirror(mirror).rotate(rotation);
			builder.set(blockPos, state);
		}

		return builder.build();
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
