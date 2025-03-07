package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.session.PlayerData;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public class InternalLocalPlayerData extends PlayerData {
	public static final Codec<InternalLocalPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("render_zones", false).forGetter(d -> d.renderZones),
		Codec.INT.optionalFieldOf("zone_render_type", 0).forGetter(d -> d.zoneRenderType),
		BlockFilter.CODEC.optionalFieldOf("zone_block_filter", BlockFilter.NONE.instance()).forGetter(d -> d.zoneBlockFilter)
	).apply(instance, InternalLocalPlayerData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, InternalLocalPlayerData> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.BOOL,
		d -> d.renderZones,
		ByteBufCodecs.VAR_INT,
		d -> d.zoneRenderType,
		BlockFilter.STREAM_CODEC,
		d -> d.zoneBlockFilter,
		InternalLocalPlayerData::new
	);

	public boolean renderZones;
	public int zoneRenderType;
	public BlockFilter zoneBlockFilter;
	public Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;

	InternalLocalPlayerData() {
		super(InternalPlayerData.LOCAL);
		this.renderZones = false;
		this.zoneRenderType = 0;
		this.zoneBlockFilter = BlockFilter.NONE.instance();
	}

	private InternalLocalPlayerData(
		boolean renderZones,
		int zoneRenderType,
		BlockFilter zoneBlockFilter
	) {
		super(InternalPlayerData.LOCAL);
		this.renderZones = renderZones;
		this.zoneRenderType = zoneRenderType;
		this.zoneBlockFilter = zoneBlockFilter;
	}
}
