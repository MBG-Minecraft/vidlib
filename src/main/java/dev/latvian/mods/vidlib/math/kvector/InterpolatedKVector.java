package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.knumber.LiteralKNumber;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record InterpolatedKVector(KNumber progress, Easing easing, KVector from, KVector to) implements KVector, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<InterpolatedKVector> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("progress", LiteralKNumber.PROGRESS).forGetter(InterpolatedKVector::progress),
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedKVector::easing),
		KVector.CODEC.fieldOf("from").forGetter(InterpolatedKVector::from),
		KVector.CODEC.fieldOf("to").forGetter(InterpolatedKVector::to)
	).apply(instance, InterpolatedKVector::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, LiteralKNumber.PROGRESS), InterpolatedKVector::progress,
		Easing.STREAM_CODEC, InterpolatedKVector::easing,
		KVector.STREAM_CODEC, InterpolatedKVector::from,
		KVector.STREAM_CODEC, InterpolatedKVector::to,
		InterpolatedKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = ImBuilderHolder.of("Interpolated", Builder::new);

		public final ImBuilder<KNumber> progress = KNumberImBuilder.create(LiteralKNumber.PROGRESS);
		public final ImBuilder<Easing> easing = EnumImBuilder.EASING_TYPE.get();
		public final ImBuilder<KVector> from = KVectorImBuilder.create();
		public final ImBuilder<KVector> to = KVectorImBuilder.create();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KVector value) {
			if (value instanceof InterpolatedKVector v) {
				progress.set(v.progress);
				easing.set(v.easing);
				from.set(v.from);
				to.set(v.to);
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
		public boolean isValid() {
			return progress.isValid() && easing.isValid() && from.isValid() && to.isValid();
		}

		@Override
		public KVector build() {
			return new InterpolatedKVector(progress.build(), easing.build(), from.build(), to.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
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

		return a.lerp(b, easing.easeClamped(progress));
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
