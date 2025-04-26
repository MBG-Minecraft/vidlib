package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.particle.ChancedParticle;
import dev.beast.mods.shimmer.feature.skybox.Skyboxes;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AutoInit
public interface InternalServerData {
	DataType<ResourceLocation> SKYBOX = DataType.SERVER.internal("skybox", KnownCodec.SHIMMER_ID, Skyboxes.DAY_WITH_CELESTIALS)
		.save()
		.sync()
		.build();

	DataType<Boolean> IMMUTABLE_WORLD = DataType.SERVER.internal("immutable_world", KnownCodec.BOOL, false)
		.save()
		.build();

	DataType<Anchor> ANCHOR = DataType.SERVER.internal("anchor", Anchor.KNOWN_CODEC, Anchor.NONE)
		.save()
		.sync()
		.onReceived((player, anchor) -> Anchor.client = anchor)
		.build();

	DataType<Boolean> HIDE_PLUMBOBS = DataType.SERVER.internal("hide_plumbobs", KnownCodec.BOOL, false)
		.save()
		.sync()
		.build();

	DataType<List<ChancedParticle>> ENVIRONMENT_EFFECTS = DataType.SERVER.internal("environment_effects", ChancedParticle.LIST_KNOWN_CODEC, List.of())
		.save()
		.sync()
		.build();
}
