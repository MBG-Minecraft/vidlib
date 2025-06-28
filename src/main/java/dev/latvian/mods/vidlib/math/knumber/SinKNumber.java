package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public record SinKNumber(KNumber angle) implements KNumber {
	public static final SimpleRegistryType<SinKNumber> TYPE = SimpleRegistryType.dynamic("sin", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("angle", KNumber.ONE).forGetter(SinKNumber::angle)
	).apply(instance, SinKNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC.optional(KNumber.ZERO), SinKNumber::angle,
		SinKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Sin", Builder::new);

		public final ImBuilder<KNumber> angle = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("Angle", !angle.isValid());
			ImGui.sameLine();
			ImGui.pushID("###angle");
			update = update.or(angle.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return angle.isValid();
		}

		@Override
		public KNumber build() {
			return new SinKNumber(angle.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var angle = this.angle.get(ctx);

		if (angle == null) {
			return null;
		}

		return Math.sin(Math.toRadians(angle));
	}
}
