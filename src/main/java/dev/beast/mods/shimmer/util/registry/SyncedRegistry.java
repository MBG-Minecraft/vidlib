package dev.beast.mods.shimmer.util.registry;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.clock.Clock;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.location.Location;
import dev.beast.mods.shimmer.feature.skybox.SkyboxData;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncedRegistry<V>(RegistryReference.IdHolder<V> registry, StreamCodec<? super RegistryFriendlyByteBuf, V> value, Callback callback) {
	public interface Callback {
		void run(Player player);
	}

	public static final Map<ResourceLocation, SyncedRegistry<?>> ALL = new LinkedHashMap<>();

	public static <V> void add(RegistryReference.IdHolder<V> registry, StreamCodec<? super RegistryFriendlyByteBuf, V> value, Callback callback) {
		ALL.put(registry.id, new SyncedRegistry<>(registry, value, callback));
	}

	public static <V> void add(RegistryReference.IdHolder<V> registry, StreamCodec<? super RegistryFriendlyByteBuf, V> value) {
		add(registry, value, null);
	}

	@AutoInit
	public static void bootstrap() {
		add(Location.REGISTRY, Location.DIRECT_STREAM_CODEC);
		add(ClockFont.REGISTRY, ClockFont.DIRECT_STREAM_CODEC);
		add(Clock.REGISTRY, Clock.DIRECT_STREAM_CODEC);
		add(ClockInstance.REGISTRY, ClockInstance.DIRECT_STREAM_CODEC);
		add(SkyboxData.REGISTRY, SkyboxData.DIRECT_STREAM_CODEC, player -> player.shimmer$sessionData().updateSkyboxes());
		add(Cutscene.REGISTRY, Cutscene.DIRECT_STREAM_CODEC);
		add(ZoneContainer.REGISTRY, ZoneContainer.DIRECT_STREAM_CODEC, player -> player.shimmer$sessionData().updateZones(player.level()));
	}

	@Override
	public String toString() {
		return registry.toString();
	}
}
