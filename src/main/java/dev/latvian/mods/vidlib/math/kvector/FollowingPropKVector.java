package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingPropKVector(int prop, PositionType positionType) implements KVector, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<FollowingPropKVector> TYPE = SimpleRegistryType.dynamic("following_prop", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.INT.fieldOf("prop").forGetter(FollowingPropKVector::prop),
		PositionType.CODEC.optionalFieldOf("position_type", PositionType.CENTER).forGetter(FollowingPropKVector::positionType)
	).apply(instance, FollowingPropKVector::new)), CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FollowingPropKVector::prop,
		PositionType.STREAM_CODEC, FollowingPropKVector::positionType,
		FollowingPropKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Following Prop", Builder::new);

		public final ImInt prop = new ImInt(0);
		public final ImBuilder<PositionType> positionType = PositionType.BUILDER_TYPE.get();

		public Builder() {
			this.positionType.set(PositionType.CENTER);
		}

		@Override
		public void set(KVector value) {
			if (value instanceof FollowingPropKVector v) {
				prop.set(v.prop);
				positionType.set(v.positionType);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("Prop ID");
			ImGui.sameLine();
			ImGui.inputInt("###prop-id", prop);
			update = update.orItemEdit();

			update = update.or(positionType.imguiKey(graphics, "Position Type", "position-type"));
			return update;
		}

		@Override
		public boolean isValid() {
			return prop.get() != 0 && positionType.isValid();
		}

		@Override
		public KVector build() {
			return new FollowingPropKVector(prop.get(), positionType.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var p = ctx.level.getProps().levelProps.get(prop);
		return p == null ? null : p.getPos(positionType);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
