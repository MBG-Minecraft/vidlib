package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ScaledKVector(KVector a, KVector b) implements KVector {
	public static final SimpleRegistryType<ScaledKVector> TYPE = SimpleRegistryType.dynamic("scaled", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KVector.CODEC.fieldOf("a").forGetter(ScaledKVector::a),
		KVector.CODEC.fieldOf("b").forGetter(ScaledKVector::b)
	).apply(instance, ScaledKVector::new)), CompositeStreamCodec.of(
		KVector.STREAM_CODEC, ScaledKVector::a,
		KVector.STREAM_CODEC, ScaledKVector::b,
		ScaledKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Scaled (a * b)", Builder::new);

		public final ImBuilder<KVector> a = KVectorImBuilder.create();
		public final ImBuilder<KVector> b = KVectorImBuilder.create();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("A");
			ImGui.sameLine();
			ImGui.pushID("###a");
			update = update.or(a.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("B");
			ImGui.sameLine();
			ImGui.pushID("###b");
			update = update.or(b.imgui(graphics));
			ImGui.popID();

			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public KVector build() {
			return a.build().scale(b.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);

		if (a == null || b == null) {
			return null;
		}

		return new Vec3(a.x * b.x, a.y * b.y, a.z * b.z);
	}
}
