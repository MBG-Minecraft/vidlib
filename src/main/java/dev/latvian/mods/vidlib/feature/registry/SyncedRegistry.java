package dev.latvian.mods.vidlib.feature.registry;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncedRegistry<V>(VLRegistry<V> registry, StreamCodec<? super RegistryFriendlyByteBuf, V> value, Callback callback) {
	public interface Callback {
		void run(Player player);
	}

	public static final Map<ResourceLocation, SyncedRegistry<?>> ALL = new LinkedHashMap<>();

	public static <V> void add(VLRegistry<V> registry, StreamCodec<? super RegistryFriendlyByteBuf, V> value, Callback callback) {
		ALL.put(registry.id, new SyncedRegistry<>(registry, value, callback));
	}

	public static <V> void add(VLRegistry<V> registry, StreamCodec<? super RegistryFriendlyByteBuf, V> value) {
		add(registry, value, null);
	}

	@AutoInit
	public static void bootstrap() {
		add(Location.REGISTRY, Location.DIRECT_STREAM_CODEC);
		add(Cutscene.REGISTRY, Cutscene.DIRECT_STREAM_CODEC);
		add(ZoneContainer.REGISTRY, ZoneContainer.DIRECT_STREAM_CODEC, player -> player.vl$sessionData().updateZones(player.level()));
	}

	@Override
	public String toString() {
		return registry.toString();
	}
}
