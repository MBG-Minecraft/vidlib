package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AutoInit
public interface InternalServerData {
	DataKey<ResourceLocation> SKYBOX = DataKey.SERVER.builder("skybox", RegisteredDataType.ID, Skyboxes.DAY_WITH_CELESTIALS)
		.save()
		.sync()
		.build();

	DataKey<Boolean> IMMUTABLE_WORLD = DataKey.SERVER.builder("immutable_world", RegisteredDataType.BOOL, false)
		.save()
		.build();

	DataKey<Anchor> ANCHOR = DataKey.SERVER.builder("anchor", Anchor.REGISTERED_DATA_TYPE, Anchor.NONE)
		.save()
		.sync()
		.onReceived((player, anchor) -> Anchor.client = anchor)
		.build();

	DataKey<Boolean> HIDE_PLUMBOBS = DataKey.SERVER.builder("hide_plumbobs", RegisteredDataType.BOOL, false)
		.save()
		.sync()
		.build();

	DataKey<List<ChancedParticle>> ENVIRONMENT_EFFECTS = DataKey.SERVER.builder("environment_effects", ChancedParticle.LIST_KNOWN_CODEC, List.of())
		.save()
		.sync()
		.build();
}
