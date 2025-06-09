package dev.latvian.mods.vidlib.feature.bulk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public record ReplaceSphereBlocks(BlockPos start, BlockPos end, Vec3 center, double radius, BlockState state) implements BulkLevelModification {
	public static final SimpleRegistryType<ReplaceSphereBlocks> TYPE = SimpleRegistryType.dynamic("sphere_blocks", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("start").forGetter(ReplaceSphereBlocks::start),
		BlockPos.CODEC.fieldOf("end").forGetter(ReplaceSphereBlocks::end),
		MCCodecs.VEC3.fieldOf("center").forGetter(ReplaceSphereBlocks::center),
		Codec.DOUBLE.fieldOf("radius").forGetter(ReplaceSphereBlocks::radius),
		MCCodecs.BLOCK_STATE.fieldOf("state").forGetter(ReplaceSphereBlocks::state)
	).apply(instance, ReplaceSphereBlocks::new)), CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC, ReplaceSphereBlocks::start,
		BlockPos.STREAM_CODEC, ReplaceSphereBlocks::end,
		MCStreamCodecs.VEC3, ReplaceSphereBlocks::center,
		ByteBufCodecs.DOUBLE, ReplaceSphereBlocks::radius,
		MCStreamCodecs.BLOCK_STATE, ReplaceSphereBlocks::state,
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
