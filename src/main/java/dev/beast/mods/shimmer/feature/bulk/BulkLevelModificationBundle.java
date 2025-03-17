package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record BulkLevelModificationBundle(List<BulkLevelModification> list) implements BulkLevelModification {
	public static final SimpleRegistryType<BulkLevelModificationBundle> TYPE = SimpleRegistryType.dynamic(Shimmer.id("bundle"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BulkLevelModification.REGISTRY.valueCodec().listOf().fieldOf("list").forGetter(BulkLevelModificationBundle::list)
	).apply(instance, BulkLevelModificationBundle::new)), BulkLevelModification.REGISTRY.valueStreamCodec().list().map(BulkLevelModificationBundle::new, BulkLevelModificationBundle::list));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
		for (var modification : list) {
			modification.collectSections(level, sections);
		}
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
		for (var modification : list) {
			modification.apply(blocks);
		}
	}

	@Override
	public BulkLevelModification optimize() {
		var tempList = new ArrayList<BulkLevelModification>(list.size());

		for (var m : list) {
			var o = m.optimize();

			if (o instanceof BulkLevelModificationBundle bundle) {
				tempList.addAll(bundle.list());
			} else {
				tempList.add(o);
			}
		}

		return BulkLevelModification.allOf(tempList);
	}
}
