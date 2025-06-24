package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingPropWorldVector(int prop, PositionType positionType) implements WorldVector {
	public static final SimpleRegistryType<FollowingPropWorldVector> TYPE = SimpleRegistryType.dynamic("following_prop", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.INT.fieldOf("prop").forGetter(FollowingPropWorldVector::prop),
		PositionType.CODEC.optionalFieldOf("position_type", PositionType.CENTER).forGetter(FollowingPropWorldVector::positionType)
	).apply(instance, FollowingPropWorldVector::new)), CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FollowingPropWorldVector::prop,
		PositionType.STREAM_CODEC, FollowingPropWorldVector::positionType,
		FollowingPropWorldVector::new
	));

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Following Prop", Builder::new);

		public final ImInt prop = new ImInt(0);
		public final PositionType[] positionType = {PositionType.CENTER};

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("Prop ID");
			ImGui.sameLine();
			ImGui.inputInt("###prop-id", prop);
			update = update.orItemEdit();

			ImGui.alignTextToFramePadding();
			ImGui.text("Position Type");
			ImGui.sameLine();
			update = update.or(graphics.combo("###position-type", "", positionType, PositionType.VALUES));

			return update;
		}

		@Override
		public boolean isValid() {
			return prop.get() != 0;
		}

		@Override
		public WorldVector build() {
			return new FollowingPropWorldVector(prop.get(), positionType[0]);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var p = ctx.level.getProps().levelProps.get(prop);
		return p == null ? null : p.getPos(positionType);
	}
}
