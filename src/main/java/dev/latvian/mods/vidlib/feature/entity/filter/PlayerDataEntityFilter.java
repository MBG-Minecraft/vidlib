package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record PlayerDataEntityFilter(DataKey<PlayerSessionData, ?> dataKey) implements EntityFilter {
	public static final DynamicType<RegistryFriendlyByteBuf, EntityFilter> TYPE = DynamicType.create(
		"player_data",
		"key",
		DataKey.PLAYER.dataKeyCodec,
		DataKey.PLAYER.dataKeyStreamCodec,
		PlayerDataEntityFilter::new,
		PlayerDataEntityFilter::dataKey
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, EntityFilter> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		var data = entity instanceof Player player ? PlayerSessionData.of(player).dataMap.getOptional(dataKey) : null;

		if (data == null) {
			return false;
		} else if (data instanceof Boolean b) {
			return b;
		}

		var num = dataKey.type.toNumber(Cast.to(data));
		return (num == null ? 0D : num instanceof Double d ? d : num.doubleValue()) != 0D;
	}

	@Override
	public boolean isStringLiteral() {
		return true;
	}

	@Override
	@NotNull
	public String toString() {
		return "player_data:" + dataKey.id;
	}
}
