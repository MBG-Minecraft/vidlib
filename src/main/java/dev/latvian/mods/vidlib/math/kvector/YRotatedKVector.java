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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record YRotatedKVector(KVector vector, KNumber angle) implements KVector, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<YRotatedKVector> TYPE = SimpleRegistryType.dynamic("y_rotated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KVector.CODEC.fieldOf("vector").forGetter(YRotatedKVector::vector),
		KNumber.CODEC.fieldOf("angle").forGetter(YRotatedKVector::angle)
	).apply(instance, YRotatedKVector::new)), CompositeStreamCodec.of(
		KVector.STREAM_CODEC, YRotatedKVector::vector,
		KNumber.STREAM_CODEC, YRotatedKVector::angle,
		YRotatedKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = ImBuilderHolder.of("Y-Rotated", Builder::new);

		public final ImBuilder<KVector> vector = KVectorImBuilder.create();
		public final ImBuilder<KNumber> angle = KNumberImBuilder.create(5D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KVector value) {
			if (value instanceof YRotatedKVector v) {
				vector.set(v.vector);
				angle.set(v.angle);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(vector.imguiKey(graphics, "Vector", "vector"));
			update = update.or(angle.imguiKey(graphics, "Angle", "angle"));
			return update;
		}

		@Override
		public boolean isValid() {
			return vector.isValid() && angle.isValid();
		}

		@Override
		public KVector build() {
			return new YRotatedKVector(vector.build(), angle.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var vector = this.vector.get(ctx);

		if (vector == null) {
			return null;
		}

		var angle = this.angle.get(ctx);

		if (angle == null) {
			return null;
		}

		var a = Math.toRadians(angle);
		var sin = org.joml.Math.sin(a);
		var cos = org.joml.Math.cosFromSin(sin, a);
		return KMath.vec3(vector.x * cos + vector.z * sin, vector.y, -vector.x * sin + vector.z * cos);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
