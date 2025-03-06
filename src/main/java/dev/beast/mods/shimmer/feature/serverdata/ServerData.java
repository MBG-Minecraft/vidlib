package dev.beast.mods.shimmer.feature.serverdata;

import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class ServerData {
	static final StreamCodec<? super RegistryFriendlyByteBuf, ServerData> STREAM_CODEC = ServerDataType.STREAM_CODEC.dispatch(ServerData::type, type -> Cast.to(type.streamCodec()));
	static final StreamCodec<? super RegistryFriendlyByteBuf, List<ServerData>> LIST_STREAM_CODEC = STREAM_CODEC.apply(Cast.to(ByteBufCodecs.list()));

	private final ServerDataType<?> type;
	int changeCount;

	public ServerData(ServerDataType<?> type) {
		this.type = type;
	}

	public ServerDataType<?> type() {
		return type;
	}

	public void setChanged() {
		if (changeCount == Integer.MAX_VALUE) {
			changeCount = 0;
		} else {
			changeCount++;
		}
	}
}
