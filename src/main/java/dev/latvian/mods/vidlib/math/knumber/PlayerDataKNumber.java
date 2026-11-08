package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayerDataKNumber(DataKey<PlayerSessionData, ?> dataKey) implements KNumber {
	public static final DynamicType<RegistryFriendlyByteBuf, KNumber> TYPE = DynamicType.create(
		"player_data",
		"key",
		DataKey.PLAYER.dataKeyCodec,
		DataKey.PLAYER.dataKeyStreamCodec,
		PlayerDataKNumber::new,
		PlayerDataKNumber::dataKey
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, KNumber> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		if (!(ctx.entity instanceof Player player)) {
			return null;
		}

		var data = PlayerSessionData.of(player).dataMap.getOptional(dataKey);

		if (data == null) {
			return null;
		}

		var num = dataKey.type.toNumber(Cast.to(data));
		return num == null ? null : num instanceof Double d ? d : num.doubleValue();
	}

	@Override
	public boolean isStringLiteral() {
		return true;
	}

	@Override
	@NotNull
	public String toString() {
		return "$$" + dataKey.id;
	}
}
