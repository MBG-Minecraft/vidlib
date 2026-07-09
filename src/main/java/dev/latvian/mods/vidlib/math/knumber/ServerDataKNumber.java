package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record ServerDataKNumber(DataKey<?> dataKey) implements KNumber, ImBuilderWithHolder.Factory {
	public static final CustomRegistryType<ServerDataKNumber> TYPE = CustomRegistryType.dynamic("server_data", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(ServerDataKNumber::key)
	).apply(instance, ServerDataKNumber::new)), ByteBufCodecs.STRING_UTF8.map(ServerDataKNumber::new, ServerDataKNumber::key));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Server Data", Builder::new);

		public final EnumImBuilder<DataKey<?>> key = EnumImBuilder.of(DataKey.SERVER.all::values).nameGetter(DataKey::id).build();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof ServerDataKNumber n) {
				key.set(n.dataKey);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return key.imguiKey(graphics, "Key", "key");
		}

		@Override
		public boolean isValid() {
			return key.isValid();
		}

		@Override
		public KNumber build() {
			return new ServerDataKNumber(key.build());
		}

		@Override
		public List<NodePin> getNodePins() {
			return NodePinType.NUMBER.singleOutput;
		}
	}

	@ApiStatus.Internal
	public ServerDataKNumber(String key) {
		this(Objects.requireNonNull(DataKey.SERVER.all.get(key), "Server data key " + key + " not found"));
	}

	@Override
	public CustomRegistryType<?> type() {
		return TYPE;
	}

	public String key() {
		return dataKey.id();
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var data = ctx.getServerData(dataKey);

		if (data == null) {
			return null;
		}

		var num = dataKey.type().toNumber(Cast.to(data));
		return num == null ? null : num instanceof Double d ? d : num.doubleValue();
	}

	@Override
	@NotNull
	public String toString() {
		return "$" + key();
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
