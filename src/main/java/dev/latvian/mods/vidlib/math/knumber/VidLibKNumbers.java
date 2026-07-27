package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class VidLibKNumbers {
	public static void registerTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, KNumber> registry) {
		registry.register(ServerDataKNumber.TYPE);
		registry.register(PlayerDataKNumber.TYPE);

		KNumber.PREFIX_LIST.addSimple("$$", PlayerDataKNumber::new);
		KNumber.PREFIX_LIST.addSimple("$", ServerDataKNumber::new);
	}
}
