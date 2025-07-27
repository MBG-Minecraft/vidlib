package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vec3ImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.world.phys.Vec3;

public record FixedKVector(Vec3 vec) implements KVector, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<FixedKVector> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.VEC3S.fieldOf("vec").forGetter(FixedKVector::vec)
	).apply(instance, KVector::of)), MCStreamCodecs.VEC3.map(KVector::of, FixedKVector::vec));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Vector", Builder::new, true);

		public final Vec3ImBuilder builder = new Vec3ImBuilder();

		@Override
		public void set(KVector value) {
			if (value instanceof FixedKVector v) {
				builder.set(v.vec);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return builder.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return builder.isValid();
		}

		@Override
		public KVector build() {
			return KVector.of(builder.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		if (vec.x == 0D && vec.y == 0D && vec.z == 0D) {
			return KVector.ZERO_TYPE;
		} else if (vec.x == 1D && vec.y == 1D && vec.z == 1D) {
			return KVector.ONE_TYPE;
		} else {
			return TYPE;
		}
	}

	@Override
	public Vec3 get(KNumberContext ctx) {
		return vec;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public KVector offset(KVector other) {
		if (other instanceof FixedKVector v) {
			return KVector.of(
				vec.x + v.vec.x,
				vec.y + v.vec.y,
				vec.z + v.vec.z
			);
		}

		return KVector.super.offset(other);
	}

	@Override
	public KVector scale(KVector other) {
		if (other instanceof FixedKVector v) {
			return KVector.of(
				vec.x * v.vec.x,
				vec.y * v.vec.y,
				vec.z * v.vec.z
			);
		}

		return KVector.super.scale(other);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
