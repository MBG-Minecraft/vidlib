package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.skybox.Skyboxes;
import net.minecraft.resources.ResourceLocation;

@AutoInit
public interface InternalServerData {
	DataType<ResourceLocation> SKYBOX = DataType.SERVER.internal("skybox", Skyboxes.DEFAULT)
		.save(ShimmerCodecs.SHIMMER_ID)
		.sync(ShimmerStreamCodecs.SHIMMER_ID)
		.build();
}
