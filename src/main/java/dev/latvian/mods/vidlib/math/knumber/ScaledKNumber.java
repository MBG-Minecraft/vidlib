package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ScaledKNumber(KNumber a, KNumber b) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<ScaledKNumber> TYPE = SimpleRegistryType.dynamic("scaled", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("a").forGetter(ScaledKNumber::a),
		KNumber.CODEC.fieldOf("b").forGetter(ScaledKNumber::b)
	).apply(instance, ScaledKNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, ScaledKNumber::a,
		KNumber.STREAM_CODEC, ScaledKNumber::b,
		ScaledKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Scaled (a * b)", Builder::new);

		public static final List<NodePin> PINS = List.of(
			NodePinType.NUMBER.required("A"),
			NodePinType.NUMBER.required("B"),
			NodePinType.NUMBER.output("Out")
		);

		public final ImBuilder<KNumber> a = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> b = KNumberImBuilder.create(0D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

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
		public ImUpdate nodeImgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public KNumber build() {
			return a.build().scale(b.build());
		}

		@Override
		public List<NodePin> getNodePins() {
			return PINS;
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
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
