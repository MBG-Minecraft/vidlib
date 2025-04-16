package dev.latvian.mods.vidlib.feature.registry;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SyncRegistryPayload(SyncedRegistry<?> registry, Map<?, ?> values) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncRegistryPayload> TYPE = VidLibPacketType.internal("sync_registry", new StreamCodec<>() {
		@Override
		public SyncRegistryPayload decode(RegistryFriendlyByteBuf buf) {
			var registry = SyncedRegistry.ALL.get(ID.idFromString(buf.readUtf()));
			int size = buf.readVarInt();

			if (size == 0) {
				return new SyncRegistryPayload(registry, Map.of());
			}

			var map = new HashMap<ResourceLocation, Object>(size);

			for (int i = 0; i < size; i++) {
				try {
					var id = ID.idFromString(buf.readUtf());
					var value = registry.value().decode(buf);
					map.put(id, value);
				} catch (Exception ex) {
					VidLib.LOGGER.error("Failed to decode registry value #" + i, ex);
				}
			}

			return new SyncRegistryPayload(registry, map);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, SyncRegistryPayload value) {
			buf.writeUtf(ID.idToString(value.registry.registry().id));

			buf.writeVarInt(value.values.size());

			for (var entry : value.values.entrySet()) {
				Object key = null;

				try {
					key = entry.getKey();
					buf.writeUtf(ID.idToString(Cast.to(key)));
					value.registry.value().encode(buf, Cast.to(entry.getValue()));
				} catch (Exception ex) {
					VidLib.LOGGER.error("Failed to encode registry value '" + key + "': " + entry, ex);
				}
			}
		}
	});

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().syncRegistry(ctx.player(), registry, Cast.to(values));
	}
}
