package dev.beast.mods.shimmer.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.session.PlayerDataType;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class InternalPlayerData {
	public static final PlayerDataType<InternalGlobalPlayerData> GLOBAL = PlayerDataType.builder(Shimmer.id("global"), InternalGlobalPlayerData::new)
		.save(RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.optionalFieldOf("display_name").forGetter(d -> d.displayName),
			ItemStack.OPTIONAL_CODEC.optionalFieldOf("hat", ItemStack.EMPTY).forGetter(d -> d.hat)
		).apply(instance, InternalGlobalPlayerData::new)))
		.sync(StreamCodec.composite(
			ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),
			d -> d.displayName,
			ItemStack.OPTIONAL_STREAM_CODEC,
			d -> d.hat,
			InternalGlobalPlayerData::new
		))
		.syncToAllClients()
		.build();

	public static final PlayerDataType<InternalLocalPlayerData> LOCAL = PlayerDataType.builder(Shimmer.id("local"), InternalLocalPlayerData::new)
		.save(RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("render_zones", false).forGetter(d -> d.renderZones)
		).apply(instance, InternalLocalPlayerData::new)))
		.sync(StreamCodec.composite(
			ByteBufCodecs.BOOL,
			d -> d.renderZones,
			InternalLocalPlayerData::new
		))
		.build();

	public static void bootstrap() {
	}
}
