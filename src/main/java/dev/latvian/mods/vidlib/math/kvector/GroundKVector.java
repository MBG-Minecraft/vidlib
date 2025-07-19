package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record GroundKVector(KVector vector) implements KVector {
	public static final SimpleRegistryType<GroundKVector> TYPE = SimpleRegistryType.dynamic("ground", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KVector.CODEC.fieldOf("vector").forGetter(GroundKVector::vector)
	).apply(instance, GroundKVector::new)), CompositeStreamCodec.of(
		KVector.STREAM_CODEC, GroundKVector::vector,
		GroundKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Ground", Builder::new);

		public final ImBuilder<KVector> vector = KVectorImBuilder.create();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			ImGui.text("Vector");
			ImGui.sameLine();
			ImGui.pushID("###vector");
			update = update.or(vector.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return vector.isValid();
		}

		@Override
		public KVector build() {
			return new GroundKVector(vector.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var pos = vector.get(ctx);

		if (pos == null) {
			return null;
		}

		var bpos = new BlockPos.MutableBlockPos(pos.x, pos.y + 0.001D, pos.z);
		BlockState state;

		while ((state = ctx.level.getBlockState(bpos)).isAir()) {
			bpos.move(0, -1, 0);

			if (ctx.level.isOutsideBuildHeight(bpos.getY())) {
				return null;
			}
		}

		//
		return new Vec3(pos.x, bpos.getY() + state.getCollisionShape(ctx.level, bpos).max(Direction.Axis.Y), pos.z);
	}
}
