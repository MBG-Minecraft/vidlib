package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

public record ScaledKNumber(KNumber a, KNumber b) implements KNumber, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<ScaledKNumber> TYPE = SimpleRegistryType.dynamic("scaled", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("a").forGetter(ScaledKNumber::a),
		KNumber.CODEC.fieldOf("b").forGetter(ScaledKNumber::b)
	).apply(instance, ScaledKNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, ScaledKNumber::a,
		KNumber.STREAM_CODEC, ScaledKNumber::b,
		ScaledKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Scaled (a * b)", Builder::new);

		public final ImBuilder<KNumber> a = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> b = KNumberImBuilder.create(0D);

		@Override
		public void set(KNumber value) {
			if (value instanceof ScaledKNumber n) {
				a.set(n.a);
				b.set(n.b);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(a.imguiKey(graphics, "A", "a"));
			update = update.or(b.imguiKey(graphics, "B", "b"));
			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public KNumber build() {
			return a.build().scale(b.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);

		if (a == null || b == null) {
			return null;
		}

		return a * b;
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
