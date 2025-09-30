package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

public record SinKNumber(KNumber angle) implements KNumber, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<SinKNumber> TYPE = SimpleRegistryType.dynamic("sin", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("angle", KNumber.ONE).forGetter(SinKNumber::angle)
	).apply(instance, SinKNumber::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), SinKNumber::angle,
		SinKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Sin", Builder::new);

		public final ImBuilder<KNumber> angle = KNumberImBuilder.create(0D);

		@Override
		public void set(KNumber value) {
			if (value instanceof SinKNumber n) {
				angle.set(n.angle);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(angle.imguiKey(graphics, "Angle", "angle"));
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

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
