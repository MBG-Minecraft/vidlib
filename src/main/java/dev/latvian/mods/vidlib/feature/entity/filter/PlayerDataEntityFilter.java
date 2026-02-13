package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PlayerDataEntityFilter(DataKey<?> dataKey) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<PlayerDataEntityFilter> TYPE = SimpleRegistryType.dynamic("player_data", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(PlayerDataEntityFilter::key)
	).apply(instance, PlayerDataEntityFilter::new)), ByteBufCodecs.STRING_UTF8.map(PlayerDataEntityFilter::new, PlayerDataEntityFilter::key));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("Player Data", Builder::new);

		public final EnumImBuilder<DataKey<?>> key = new EnumImBuilder<>(DataKey.PLAYER.all.values()).withNameGetter(DataKey::id);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof PlayerDataEntityFilter n) {
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
			return new PlayerDataEntityFilter(key.build());
		}
	}

	@ApiStatus.Internal
	public PlayerDataEntityFilter(String key) {
		this(Objects.requireNonNull(DataKey.PLAYER.all.get(key), "Player data key " + key + " not found"));
	}

	@Override
	public SimpleRegistryType<?> type() {
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
		return "$$" + key();
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
