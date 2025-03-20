package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.skybox.Skyboxes;
import net.minecraft.resources.ResourceLocation;

@AutoInit
public interface InternalServerData {
	DataType<ResourceLocation> SKYBOX = DataType.SERVER.internal("skybox", KnownCodec.SHIMMER_ID, Skyboxes.DEFAULT)
		.save()
		.sync()
		.build();
}
