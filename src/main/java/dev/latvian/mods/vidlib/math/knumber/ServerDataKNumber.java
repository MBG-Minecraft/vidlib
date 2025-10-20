package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.data.DataKey;
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
import java.util.function.Supplier;

public record ServerDataKNumber(String key, Supplier<DataKey<?>> dataKey) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<ServerDataKNumber> TYPE = SimpleRegistryType.dynamic("server_data", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(ServerDataKNumber::key)
	).apply(instance, ServerDataKNumber::new)), ByteBufCodecs.STRING_UTF8.map(ServerDataKNumber::new, ServerDataKNumber::key));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Server Data", Builder::new);

		public final ImString key = ImGuiUtils.resizableString();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof ServerDataKNumber n) {
				key.set(n.key);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			// TODO: Replace with combo of all server data keys
			ImGui.inputText("###key", key);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return key.isNotEmpty() && DataKey.SERVER.all.containsKey(key.get());
		}

		@Override
		public KNumber build() {
			return new ServerDataKNumber(key.get());
		}

		@Override
		public List<NodePin> getNodePins() {
			return NodePinType.NUMBER.singleOutput;
		}
	}

	public ServerDataKNumber(String key) {
		this(key, Lazy.of(() -> DataKey.SERVER.all.get(key)));
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var dk = dataKey.get();
		var data = ctx.getServerData(dk);

		if (data == null) {
			return null;
		}

		var num = dk.type().toNumber(Cast.to(data));
		return num == null ? null : num instanceof Double d ? d : num.doubleValue();
	}

	@Override
	@NotNull
	public String toString() {
		return "$" + key;
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
