package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class PlayerData {
	static final StreamCodec<? super RegistryFriendlyByteBuf, PlayerData> STREAM_CODEC = PlayerDataType.STREAM_CODEC.dispatch(PlayerData::type, type -> Cast.to(type.streamCodec()));
	static final StreamCodec<? super RegistryFriendlyByteBuf, List<PlayerData>> LIST_STREAM_CODEC = STREAM_CODEC.apply(Cast.to(ByteBufCodecs.list()));

	private final PlayerDataType<?> type;
	int changeCount;

	public PlayerData(PlayerDataType<?> type) {
		this.type = type;
	}

	public PlayerDataType<?> type() {
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
