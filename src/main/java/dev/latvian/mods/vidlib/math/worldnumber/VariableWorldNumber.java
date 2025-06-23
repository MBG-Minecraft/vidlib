package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VariableWorldNumber(String name) implements WorldNumber {
	public static final SimpleRegistryType<VariableWorldNumber> TYPE = SimpleRegistryType.dynamic("variable", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableWorldNumber::name)
	).apply(instance, VariableWorldNumber::new)), ByteBufCodecs.STRING_UTF8.map(VariableWorldNumber::new, VariableWorldNumber::name));

	public static class Builder implements WorldNumberImBuilder {
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("Variable", Builder::new);

		public final ImString variable = ImGuiUtils.resizableString();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.pushItemWidth(-1F);
			ImGui.inputText("###variable", variable);
			ImGui.popItemWidth();
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return variable.isNotEmpty();
		}

		@Override
		public WorldNumber build() {
			return new VariableWorldNumber(variable.get());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(WorldNumberContext ctx) {
		var num = ctx.variables.numbers().get(name);
		return num == null ? null : num.get(ctx);
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
}
