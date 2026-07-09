package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ServerDataEntityFilter(DataKey<?> dataKey) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static final DynamicType<RegistryFriendlyByteBuf, EntityFilter> TYPE = CustomRegistryType.dynamic("server_data", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(ServerDataEntityFilter::key)
	).apply(instance, ServerDataEntityFilter::new)), ByteBufCodecs.STRING_UTF8.map(ServerDataEntityFilter::new, ServerDataEntityFilter::key));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("Server Data", Builder::new);

		public final EnumImBuilder<DataKey<?>> key = EnumImBuilder.of(DataKey.SERVER.all::values).nameGetter(DataKey::id).build();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof ServerDataEntityFilter n) {
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
		public EntityFilter build() {
			return new ServerDataEntityFilter(key.build());
		}
	}

	@ApiStatus.Internal
	public ServerDataEntityFilter(String key) {
		this(Objects.requireNonNull(DataKey.SERVER.all.get(key), "Server data key " + key + " not found"));
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, EntityFilter> type() {
		return TYPE;
	}

	public String key() {
		return dataKey.id();
	}

	@Override
	public boolean test(Entity entity) {
		var data = entity.level().getOptional(dataKey);

		if (data == null) {
			return false;
		} else if (data instanceof Boolean b) {
			return b;
		}

		var num = dataKey.type().toNumber(Cast.to(data));
		return (num == null ? 0D : num instanceof Double d ? d : num.doubleValue()) != 0D;
	}

	@Override
	@NotNull
	public String toString() {
		return "$" + key();
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
