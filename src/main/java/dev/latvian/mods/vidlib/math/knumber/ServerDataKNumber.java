package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ServerDataKNumber(DataKey<?> dataKey) implements KNumber, ImBuilderWithHolder.Factory {
	public static final DynamicType<RegistryFriendlyByteBuf, KNumber> TYPE = DynamicType.create(
		"server_data",
		"key",
		Codec.STRING,
		ByteBufCodecs.STRING_UTF8,
		ServerDataKNumber::new,
		ServerDataKNumber::dataKeyId
	);

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
	}

	@ApiStatus.Internal
	public ServerDataKNumber(String key) {
		this(Objects.requireNonNull(DataKey.SERVER.all.get(key), "Server data key " + key + " not found"));
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, KNumber> type() {
		return TYPE;
	}

	public String dataKeyId() {
		return dataKey.id();
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var dataMap = ctx.level.getDataMap();

		if (dataMap == null) {
			return null;
		}

		var data = dataMap.get(dataKey);

		if (data == null) {
			return null;
		}

		var num = dataKey.type().toNumber(Cast.to(data));
		return num == null ? null : num instanceof Double d ? d : num.doubleValue();
	}

	@Override
	public boolean isStringLiteral() {
		return true;
	}

	@Override
	@NotNull
	public String toString() {
		return "$" + dataKeyId();
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
