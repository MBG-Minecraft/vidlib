package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingEntityKVector(EntityFilter entity, PositionType positionType) implements KVector {
	public static final SimpleRegistryType<FollowingEntityKVector> TYPE = SimpleRegistryType.dynamic("following_entity", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("entity").forGetter(FollowingEntityKVector::entity),
		PositionType.CODEC.optionalFieldOf("position_type", PositionType.CENTER).forGetter(FollowingEntityKVector::positionType)
	).apply(instance, FollowingEntityKVector::new)), CompositeStreamCodec.of(
		EntityFilter.STREAM_CODEC, FollowingEntityKVector::entity,
		PositionType.STREAM_CODEC, FollowingEntityKVector::positionType,
		FollowingEntityKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Following Entity", Builder::new);

		public final ImBuilder<EntityFilter> entity = EntityFilterImBuilder.create();
		public final PositionType[] positionType = {PositionType.CENTER};

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("Entity");
			ImGui.sameLine();
			ImGui.pushID("###entity");
			update = update.or(entity.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Position Type");
			ImGui.sameLine();
			update = update.or(graphics.combo("###position-type", "", positionType, PositionType.VALUES));

			return update;
		}

		@Override
		public boolean isValid() {
			return entity.isValid() && entity.build() != EntityFilter.NONE.instance();
		}

		@Override
		public KVector build() {
			return KVector.following(entity.build(), positionType[0]);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var e = entity.getFirst(ctx.level);
		return e == null ? null : e.getPosition(positionType);
	}
}
