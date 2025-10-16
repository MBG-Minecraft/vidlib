package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingEntityKVector(EntityFilter entity, PositionType positionType) implements KVector, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<FollowingEntityKVector> TYPE = SimpleRegistryType.dynamic("following_entity", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("entity").forGetter(FollowingEntityKVector::entity),
		PositionType.CODEC.optionalFieldOf("position_type", PositionType.CENTER).forGetter(FollowingEntityKVector::positionType)
	).apply(instance, FollowingEntityKVector::new)), CompositeStreamCodec.of(
		EntityFilter.STREAM_CODEC, FollowingEntityKVector::entity,
		PositionType.STREAM_CODEC, FollowingEntityKVector::positionType,
		FollowingEntityKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = ImBuilderHolder.of("Following Entity", Builder::new);

		public final ImBuilder<EntityFilter> entity = EntityFilterImBuilder.create();
		public final ImBuilder<PositionType> positionType = PositionType.BUILDER_TYPE.get();

		public Builder() {
			this.positionType.set(PositionType.CENTER);
		}

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KVector value) {
			if (value instanceof FollowingEntityKVector v) {
				entity.set(v.entity);
				positionType.set(v.positionType);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(entity.imguiKey(graphics, "Entity", "entity"));
			update = update.or(positionType.imguiKey(graphics, "Position Type", "position-type"));
			return update;
		}

		@Override
		public boolean isValid() {
			return entity.isValid() && entity.build() != EntityFilter.NONE.instance() && positionType.isValid();
		}

		@Override
		public KVector build() {
			return KVector.following(entity.build(), positionType.build());
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

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
