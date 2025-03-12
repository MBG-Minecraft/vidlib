package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.icon.IconHolder;
import dev.beast.mods.shimmer.feature.zone.ZoneRenderType;
import dev.beast.mods.shimmer.util.Empty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

@AutoInit
public interface InternalPlayerData {
	DataType<Component> NICKNAME = DataType.PLAYER.internal("nickname", Empty.COMPONENT)
		.save(ComponentSerialization.CODEC)
		.sync(ComponentSerialization.STREAM_CODEC)
		.syncToAllClients()
		.build();

	DataType<IconHolder> PLUMBOB = DataType.PLAYER.internal("plumbob", IconHolder.EMPTY)
		.save(IconHolder.CODEC)
		.sync(IconHolder.STREAM_CODEC)
		.syncToAllClients()
		.build();

	DataType<Boolean> SHOW_ZONES = DataType.PLAYER.internal("show_zones", false)
		.identity()
		.save(Codec.BOOL)
		.sync(ByteBufCodecs.BOOL)
		.build();

	DataType<ZoneRenderType> ZONE_RENDER_TYPE = DataType.PLAYER.internal("zone_render_type", ZoneRenderType.NORMAL)
		.identity()
		.save(ZoneRenderType.CODEC)
		.sync(ZoneRenderType.STREAM_CODEC)
		.build();

	DataType<BlockFilter> ZONE_BLOCK_FILTER = DataType.PLAYER.internal("zone_block_filter", BlockFilter.ANY.instance())
		.save(BlockFilter.CODEC)
		.sync(BlockFilter.STREAM_CODEC)
		.onReceived(player -> player.shimmer$sessionData().refreshBlockZones())
		.build();
}
