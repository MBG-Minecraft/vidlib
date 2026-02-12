package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.UnitSupplier;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record PlayerDataEntityFilter(String key, Supplier<DataKey<?>> dataKey) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<PlayerDataEntityFilter> TYPE = SimpleRegistryType.dynamic("player_data", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(PlayerDataEntityFilter::key)
	).apply(instance, PlayerDataEntityFilter::new)), ByteBufCodecs.STRING_UTF8.map(PlayerDataEntityFilter::new, PlayerDataEntityFilter::key));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("Player Data", Builder::new);

		public final ImString key = ImGuiUtils.resizableString();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof PlayerDataEntityFilter n) {
				key.set(n.key);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.inputText("###key", key);
			update = update.orItemEdit();
			return update;
		}

		@Override
		public boolean isValid() {
			return key.isNotEmpty() && DataKey.PLAYER.all.containsKey(key.get());
		}

		@Override
		public EntityFilter build() {
			return new PlayerDataEntityFilter(key.get());
		}
	}

	public PlayerDataEntityFilter(String key) {
		this(key, Lazy.of(() -> DataKey.PLAYER.all.get(key)));
	}

	public PlayerDataEntityFilter(DataKey<?> key) {
		this(key.id(), new UnitSupplier<>(key));
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		if (!(entity instanceof Player player)) {
			return false;
		}

		var dk = dataKey.get();
		var data = player.getOptional(dk);

		if (data == null) {
			return false;
		} else if (data instanceof Boolean b) {
			return b;
		}

		var num = dk.type().toNumber(Cast.to(data));
		return (num == null ? 0D : num instanceof Double d ? d : num.doubleValue()) != 0D;
	}

	@Override
	@NotNull
	public String toString() {
		return "$" + key;
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
