package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VariableKVector(String name) implements KVector, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<VariableKVector> TYPE = SimpleRegistryType.dynamic("variable", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableKVector::name)
	).apply(instance, VariableKVector::new)), ByteBufCodecs.STRING_UTF8.map(VariableKVector::new, VariableKVector::name));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Variable", Builder::new);

		public final ImString name = ImGuiUtils.resizableString();

		@Override
		public void set(KVector value) {
			if (value instanceof VariableKVector v) {
				name.set(v.name);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.inputText("###name", name);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return name.isNotEmpty();
		}

		@Override
		public KVector build() {
			return new VariableKVector(name.get());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var pos = ctx.getVector(name);

		if (pos == null) {
			var num = ctx.getNumber(name);

			if (num != null) {
				var d = num.get(ctx);

				if (d == null) {
					return null;
				}

				return KMath.vec3(d, d, d);
			}
		}

		return pos == null ? null : pos.get(ctx);
	}

	@Override
	@NotNull
	public String toString() {
		return name;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}