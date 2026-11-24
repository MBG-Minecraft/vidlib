package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface VLKNumbers {
	static void register(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, KNumber> registry) {
		registry.register(GlobalDataKNumber.TYPE);
		registry.register(LevelDataKNumber.TYPE);
		registry.register(PlayerDataKNumber.TYPE);

		KNumber.PREFIX_LIST.add("global_data:", DataKey.GLOBAL.dataKeyCodec, GlobalDataKNumber::new);
		KNumber.PREFIX_LIST.add("level_data:", DataKey.LEVEL.dataKeyCodec, LevelDataKNumber::new);
		KNumber.PREFIX_LIST.add("player_data:", DataKey.PLAYER.dataKeyCodec, PlayerDataKNumber::new);
	}
}
