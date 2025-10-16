package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;

public record DynamicKVector(KNumber x, KNumber y, KNumber z) implements KVector, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<DynamicKVector> TYPE = SimpleRegistryType.dynamic("dynamic", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("x").forGetter(DynamicKVector::x),
		KNumber.CODEC.fieldOf("y").forGetter(DynamicKVector::y),
		KNumber.CODEC.fieldOf("z").forGetter(DynamicKVector::z)
	).apply(instance, DynamicKVector::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, DynamicKVector::x,
		KNumber.STREAM_CODEC, DynamicKVector::y,
		KNumber.STREAM_CODEC, DynamicKVector::z,
		DynamicKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = ImBuilderHolder.of("Dynamic", Builder::new);

		public final ImBuilder<KNumber> x = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> y = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> z = KNumberImBuilder.create(0D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KVector value) {
			if (value instanceof DynamicKVector v) {
				x.set(v.x);
				y.set(v.y);
				z.set(v.z);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.indent();
			update = update.or(x.imguiKey(graphics, "X", "x"));
			update = update.or(y.imguiKey(graphics, "Y", "y"));
			update = update.or(z.imguiKey(graphics, "Z", "z"));
			ImGui.unindent();
			return update;
		}

		@Override
		public boolean isValid() {
			return x.isValid() && y.isValid() && z.isValid();
		}

		@Override
		public KVector build() {
			return new DynamicKVector(x.build(), y.build(), z.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(KNumberContext ctx) {
		var px = x.get(ctx);
		var py = y.get(ctx);
		var pz = z.get(ctx);

		if (px == null || py == null || pz == null) {
			return null;
		}

		return KMath.vec3(px, py, pz);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
