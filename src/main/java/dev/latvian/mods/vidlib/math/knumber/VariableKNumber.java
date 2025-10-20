package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record VariableKNumber(String name) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<VariableKNumber> TYPE = SimpleRegistryType.dynamic("variable", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableKNumber::name)
	).apply(instance, VariableKNumber::new)), ByteBufCodecs.STRING_UTF8.map(VariableKNumber::new, VariableKNumber::name));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Variable", Builder::new);

		public final ImString name = ImGuiUtils.resizableString();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof VariableKNumber n) {
				name.set(n.name);
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
		public KNumber build() {
			return new VariableKNumber(name.get());
		}

		@Override
		public List<NodePin> getNodePins() {
			return NodePinType.NUMBER.singleOutput;
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var num = ctx.getNumber(name);
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

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
