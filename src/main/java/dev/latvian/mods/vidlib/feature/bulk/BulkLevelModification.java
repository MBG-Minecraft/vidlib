package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RefOptimizer;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.structure.LazyStructures;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface BulkLevelModification extends RefOptimizer<BulkLevelModification> {
	CustomRegistry<ByteBuf, BulkLevelModification> REGISTRY = CustomRegistry.<ByteBuf, BulkLevelModification>builder()
		.keys(ID.vidlib("bulk_level_modification"), VidLib.ID)
		.type(BulkLevelModification::type)
		.build();

	Codec<Ref<BulkLevelModification>> CODEC = REGISTRY.codec();
	StreamCodec<ByteBuf, Ref<BulkLevelModification>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<BulkLevelModification>> DATA_TYPE = REGISTRY.dataType();

	BulkLevelModification NONE = new BulkLevelModificationBundle(List.of());

	static void builtInTypes(CustomRegistryTypeCollector<ByteBuf, BulkLevelModification> registry) {
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
		BasicRegistryRef<Identifier, LazyStructures> templateRef,
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

	@Nullable
	default CustomRegistryType<ByteBuf, BulkLevelModification> type() {
		return null;
	}

	void collectSections(Level level, Set<SectionPos> sections);

	void apply(BlockModificationConsumer blocks);
}
