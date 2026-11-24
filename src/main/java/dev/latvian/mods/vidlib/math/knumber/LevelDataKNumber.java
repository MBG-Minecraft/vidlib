package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LevelDataKNumber(DataKey<LevelSessionData<?>, ?> dataKey) implements KNumber {
	public static final DynamicType<RegistryFriendlyByteBuf, KNumber> TYPE = DynamicType.create(
		"level_data",
		"key",
		DataKey.LEVEL.dataKeyCodec,
		DataKey.LEVEL.dataKeyStreamCodec,
		LevelDataKNumber::new,
		LevelDataKNumber::dataKey
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, KNumber> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var data = LevelSessionData.of(ctx.level).dataMap.get(dataKey);

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
		return "level_data:" + dataKey.id;
	}
}
