package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.zone.ZoneRenderType;
import dev.beast.mods.shimmer.util.Empty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

public class InternalPlayerData {
	public static final DataType<Component> NICKNAME = DataType.PLAYER.internal("nickname", Empty.COMPONENT)
		.save(ComponentSerialization.CODEC)
		.sync(ComponentSerialization.STREAM_CODEC)
		.syncToAllClients()
		.build();

	public static final DataType<ItemStack> PLUMBOB = DataType.PLAYER.internal("plumbob", ItemStack.EMPTY)
		.save(ItemStack.OPTIONAL_CODEC)
		.sync(ItemStack.OPTIONAL_STREAM_CODEC)
		.syncToAllClients()
		.build();

	public static final DataType<Boolean> SHOW_ZONES = DataType.PLAYER.internal("show_zones", false)
		.identity()
		.save(Codec.BOOL)
		.sync(ByteBufCodecs.BOOL)
		.build();

	public static final DataType<ZoneRenderType> ZONE_RENDER_TYPE = DataType.PLAYER.internal("zone_render_type", ZoneRenderType.NORMAL)
		.identity()
		.save(ZoneRenderType.CODEC)
		.sync(ZoneRenderType.STREAM_CODEC)
		.build();

	public static final DataType<BlockFilter> ZONE_BLOCK_FILTER = DataType.PLAYER.internal("zone_block_filter", BlockFilter.NONE.instance())
		.save(BlockFilter.CODEC)
		.sync(BlockFilter.STREAM_CODEC)
		.onReceived(player -> player.shimmer$sessionData().refreshBlockZones())
		.build();

	public static void bootstrap() {
	}
}
