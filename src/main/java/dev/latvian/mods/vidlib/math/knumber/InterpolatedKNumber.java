package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record InterpolatedKNumber(KNumber progress, Easing easing, KNumber from, KNumber to) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<InterpolatedKNumber> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("progress", LiteralKNumber.PROGRESS).forGetter(InterpolatedKNumber::progress),
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedKNumber::easing),
		KNumber.CODEC.fieldOf("from").forGetter(InterpolatedKNumber::from),
		KNumber.CODEC.fieldOf("to").forGetter(InterpolatedKNumber::to)
	).apply(instance, InterpolatedKNumber::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, LiteralKNumber.PROGRESS), InterpolatedKNumber::progress,
		Easing.STREAM_CODEC, InterpolatedKNumber::easing,
		KNumber.STREAM_CODEC, InterpolatedKNumber::from,
		KNumber.STREAM_CODEC, InterpolatedKNumber::to,
		InterpolatedKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Interpolated", Builder::new);

		public static final List<NodePin> PINS = List.of(
			NodePinType.NUMBER.optional("Progress"),
			NodePinType.EASING.optional("Easing"),
			NodePinType.NUMBER.required("From"),
			NodePinType.NUMBER.required("To"),
			NodePinType.NUMBER.output("Out")
		);

		public final ImBuilder<KNumber> progress = KNumberImBuilder.create(LiteralKNumber.PROGRESS);
		public final ImBuilder<Easing> easing = EnumImBuilder.EASING_TYPE.get();
		public final ImBuilder<KNumber> from = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> to = KNumberImBuilder.create(1D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof InterpolatedKNumber n) {
				progress.set(n.progress);
				easing.set(n.easing);
				from.set(n.from);
				to.set(n.to);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(progress.imguiKey(graphics, "Progress", "progress"));
			update = update.or(easing.imguiKey(graphics, "Easing", "easing"));

			if (ImGui.beginTable("###table", 2, ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.Borders)) {
				ImGui.tableNextRow();
				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);
				update = update.or(from.imguiKey(graphics, "From", "from"));
				ImGui.popItemWidth();
				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);
				update = update.or(to.imguiKey(graphics, "To", "to"));
				ImGui.popItemWidth();
				ImGui.endTable();
			}

			return update;
		}

		@Override
		public ImUpdate nodeImgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public boolean isValid() {
			return progress.isValid() && easing.isValid() && from.isValid() && to.isValid();
		}

		@Override
		public KNumber build() {
			return new InterpolatedKNumber(progress.build(), easing.build(), from.build(), to.build());
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
		var progress = this.progress.getOrNaN(ctx);

		if (Double.isNaN(progress)) {
			return null;
		}

		if (progress <= 0D) {
			return from.get(ctx);
		} else if (progress >= 1D) {
			return to.get(ctx);
		}

		var a = from.get(ctx);
		var b = to.get(ctx);

		if (a == null || b == null) {
			return null;
		}

		return KMath.lerp(easing.easeClamped(progress), a, b);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
