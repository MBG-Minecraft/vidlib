package dev.beast.mods.shimmer.util.registry;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SyncRegistryPayload(SyncedRegistry<?> registry, Map<?, ?> values) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncRegistryPayload> TYPE = ShimmerPacketType.internal("sync_registry", new StreamCodec<>() {
		@Override
		public SyncRegistryPayload decode(RegistryFriendlyByteBuf buf) {
			var registry = SyncedRegistry.ALL.get(ShimmerStreamCodecs.SHIMMER_ID.decode(buf));
			int size = buf.readVarInt();

			if (size == 0) {
				return new SyncRegistryPayload(registry, Map.of());
			}

			var map = new HashMap<ResourceLocation, Object>(size);

			for (int i = 0; i < size; i++) {
				var id = registry.registry().keyStreamCodec.decode(buf);
				var value = registry.value().decode(buf);
				map.put(id, value);
			}

			return new SyncRegistryPayload(registry, map);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, SyncRegistryPayload value) {
			ShimmerStreamCodecs.SHIMMER_ID.encode(buf, value.registry.registry().id);

			buf.writeVarInt(value.values.size());

			for (var entry : value.values.entrySet()) {
				value.registry.registry().keyStreamCodec.encode(buf, Cast.to(entry.getKey()));
				value.registry.value().encode(buf, Cast.to(entry.getValue()));
			}
		}
	});

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$sessionData().syncRegistry(ctx.player(), registry, Cast.to(values));
	}
}
