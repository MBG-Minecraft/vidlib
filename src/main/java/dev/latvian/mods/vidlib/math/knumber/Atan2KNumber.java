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

public record Atan2KNumber(KNumber x, KNumber y) implements KNumber, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<Atan2KNumber> TYPE = SimpleRegistryType.dynamic("atan2", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("x", KNumber.ZERO).forGetter(Atan2KNumber::x),
		KNumber.CODEC.optionalFieldOf("y", KNumber.ONE).forGetter(Atan2KNumber::y)
	).apply(instance, Atan2KNumber::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), Atan2KNumber::x,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ONE), Atan2KNumber::y,
		Atan2KNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Atan2", Builder::new);

		public final ImBuilder<KNumber> x = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> y = KNumberImBuilder.create(0D);

		@Override
		public void set(KNumber value) {
			if (value instanceof Atan2KNumber n) {
				x.set(n.x);
				y.set(n.y);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(x.imguiKey(graphics, "X", "x"));
			update = update.or(y.imguiKey(graphics, "Y", "y"));
			return update;
		}

		@Override
		public boolean isValid() {
			return x.isValid() && y.isValid();
		}

		@Override
		public KNumber build() {
			return new Atan2KNumber(x.build(), y.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var x = this.x.get(ctx);
		var y = this.y.get(ctx);

		if (x == null || y == null) {
			return null;
		}

		return Math.toDegrees(Math.atan2(y, x));
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
