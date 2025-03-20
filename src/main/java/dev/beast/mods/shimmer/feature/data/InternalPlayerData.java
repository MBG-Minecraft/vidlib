package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.icon.IconHolder;
import dev.beast.mods.shimmer.feature.zone.ZoneRenderType;
import dev.beast.mods.shimmer.util.Empty;
import net.minecraft.network.chat.Component;

@AutoInit
public interface InternalPlayerData {
	DataType<Boolean> SUSPENDED = DataType.PLAYER.internal("suspended", KnownCodec.BOOL, false)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<Component> NICKNAME = DataType.PLAYER.internal("nickname", KnownCodec.TEXT_COMPONENT, Empty.COMPONENT)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<IconHolder> PLUMBOB = DataType.PLAYER.internal("plumbob", IconHolder.KNOWN_CODEC, IconHolder.EMPTY)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<Clothing> CLOTHING = DataType.PLAYER.internal("clothing", Clothing.KNOWN_CODEC, Clothing.NONE)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<Boolean> SHOW_ZONES = DataType.PLAYER.internal("show_zones", KnownCodec.BOOL, false)
		.save()
		.sync()
		.build();

	DataType<ZoneRenderType> ZONE_RENDER_TYPE = DataType.PLAYER.internal("zone_render_type", ZoneRenderType.KNOWN_CODEC, ZoneRenderType.NORMAL)
		.save()
		.sync()
		.build();

	DataType<BlockFilter> ZONE_BLOCK_FILTER = DataType.PLAYER.internal("zone_block_filter", BlockFilter.KNOWN_CODEC, BlockFilter.ANY.instance())
		.save()
		.sync()
		.onReceived(player -> player.shimmer$sessionData().refreshBlockZones())
		.build();

	DataType<Float> FLIGHT_SPEED = DataType.PLAYER.internal("flight_speed", KnownCodec.FLOAT, 1F)
		.save()
		.sync()
		.build();
}
