package dev.beast.mods.shimmer.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public record ReplaceSphereBlocks(BlockPos start, BlockPos end, Vec3 center, double radius, BlockState state) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceSphereBlocks> TYPE = SimpleRegistryType.dynamic(Shimmer.id("sphere_blocks"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(ReplaceSphereBlocks::start),
		BlockPos.CODEC.fieldOf("end").forGetter(ReplaceSphereBlocks::end),
		Vec3.CODEC.fieldOf("center").forGetter(ReplaceSphereBlocks::center),
		Codec.DOUBLE.fieldOf("radius").forGetter(ReplaceSphereBlocks::radius),
		BlockState.CODEC.fieldOf("state").forGetter(ReplaceSphereBlocks::state)
	).apply(instance, ReplaceSphereBlocks::new)), CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, ReplaceSphereBlocks::start,
		BlockPos.STREAM_CODEC, ReplaceSphereBlocks::end,
		ShimmerStreamCodecs.VEC_3, ReplaceSphereBlocks::center,
		ByteBufCodecs.DOUBLE, ReplaceSphereBlocks::radius,
		ShimmerStreamCodecs.BLOCK_STATE, ReplaceSphereBlocks::state,
		ReplaceSphereBlocks::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void collectSections(Level level, Set<SectionPos> sections) {
	}

	@Override
	public void apply(BlockModificationConsumer blocks) {
	}
}
