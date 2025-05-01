package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AutoInit
public interface InternalServerData {
	DataType<ResourceLocation> SKYBOX = DataType.SERVER.builder("skybox", KnownCodec.ID, Skyboxes.DAY_WITH_CELESTIALS)
		.save()
		.sync()
		.build();

	DataType<Boolean> IMMUTABLE_WORLD = DataType.SERVER.builder("immutable_world", KnownCodec.BOOL, false)
		.save()
		.build();

	DataType<Anchor> ANCHOR = DataType.SERVER.builder("anchor", Anchor.KNOWN_CODEC, Anchor.NONE)
		.save()
		.sync()
		.onReceived((player, anchor) -> Anchor.client = anchor)
		.build();

	DataType<Boolean> HIDE_PLUMBOBS = DataType.SERVER.builder("hide_plumbobs", KnownCodec.BOOL, false)
		.save()
		.sync()
		.build();

	DataType<List<ChancedParticle>> ENVIRONMENT_EFFECTS = DataType.SERVER.builder("environment_effects", ChancedParticle.LIST_KNOWN_CODEC, List.of())
		.save()
		.sync()
		.build();
}
