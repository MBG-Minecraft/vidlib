package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public record CosKNumber(KNumber angle) implements KNumber {
	public static final SimpleRegistryType<CosKNumber> TYPE = SimpleRegistryType.dynamic("cos", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("angle", KNumber.ONE).forGetter(CosKNumber::angle)
	).apply(instance, CosKNumber::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), CosKNumber::angle,
		CosKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Cos", Builder::new);

		public final ImBuilder<KNumber> angle = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);
			update = update.or(angle.imguiKey(graphics, "Angle", "angle"));
			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return angle.isValid();
		}

		@Override
		public KNumber build() {
			return new CosKNumber(angle.build());
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

		return Math.cos(Math.toRadians(angle));
	}
}
