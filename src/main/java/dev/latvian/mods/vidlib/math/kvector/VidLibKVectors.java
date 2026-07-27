package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.klib.kvector.DynamicKVector;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.math.knumber.PlayerDataKNumber;
import dev.latvian.mods.vidlib.math.knumber.ServerDataKNumber;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class VidLibKVectors {
	public static void registerTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, KVector> registry) {
		registry.register(FollowingPropKVector.TYPE);

		KVector.PREFIX_LIST.addSimple("$$", input -> {
			var n = new PlayerDataKNumber(input).ref();
			return new DynamicKVector(n, n, n);
		});

		KVector.PREFIX_LIST.addSimple("$", input -> {
			var n = new ServerDataKNumber(input).ref();
			return new DynamicKVector(n, n, n);
		});
	}
}
