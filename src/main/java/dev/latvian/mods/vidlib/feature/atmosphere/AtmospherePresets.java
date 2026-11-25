package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecErrors;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.ColorModifier;
import net.minecraft.world.attribute.modifier.ListModifier;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AtmospherePresets {
	Identifier SKYBOX_ID = ID.vidlib("skybox");

	static EnvironmentAttributeMap.Builder gradient(int sky, int fog) {
		return EnvironmentAttributeMap.builder()
			.set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(sky))
			.set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(fog));
	}

	static EnvironmentAttributeMap.Builder screen(int color) {
		return gradient(color, color);
	}

	static EnvironmentAttributeMap.Builder skybox(SkyboxTextureId id) {
		return EnvironmentAttributeMap.builder()
			.set(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, SKYBOX_ID)
			.set(VidLibEnvironmentAttributes.SKYBOX.get(), SkyboxData.of(id));
	}

	Lazy<EnvironmentAttributeMap> DAY = Lazy.of(() -> gradient(0x72B2FD, 0xB4D1FE).build());
	Lazy<EnvironmentAttributeMap> NIGHT = Lazy.of(() -> gradient(0x00001A, 0x123A70).build());
	Lazy<EnvironmentAttributeMap> STORM = Lazy.of(() -> gradient(0x333A4C, 0x6E7C8A).build());
	Lazy<EnvironmentAttributeMap> DOOM = Lazy.of(() -> gradient(0x554C47, 0xA75A3D).build());
	Lazy<EnvironmentAttributeMap> BRIGHT_NIGHT = Lazy.of(() -> gradient(0x1C6D9E, 0x3D91A9).set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.5F).build());
	Lazy<EnvironmentAttributeMap> SPACE = Lazy.of(() -> gradient(0x00001A, 0x00001A).set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.65F).build());

	Lazy<EnvironmentAttributeMap> BLACK_SCREEN = Lazy.of(() -> screen(0x000000).build());
	Lazy<EnvironmentAttributeMap> WHITE_SCREEN = Lazy.of(() -> screen(0xFFFFFF).build());
	Lazy<EnvironmentAttributeMap> GREEN_SCREEN = Lazy.of(() -> screen(0x00FF00).build());
	Lazy<EnvironmentAttributeMap> BLUE_SCREEN = Lazy.of(() -> screen(0x0000FF).build());

	Lazy<EnvironmentAttributeMap> NO_SUN = Lazy.of(() -> gradient(0x72B2FD, 0xB4D1FE).set(VidLibEnvironmentAttributes.SUN.get(), false).build());
	Lazy<EnvironmentAttributeMap> NO_MOON = Lazy.of(() -> gradient(0x00001A, 0x123A70).set(VidLibEnvironmentAttributes.MOON.get(), false).build());
	Lazy<EnvironmentAttributeMap> ASH = Lazy.of(() -> EnvironmentAttributeMap.builder().modify(EnvironmentAttributes.AMBIENT_PARTICLES, ListModifier.append(), AmbientParticle.of(ParticleTypes.WHITE_ASH, 0.118093334F)).build());
	Lazy<EnvironmentAttributeMap> DARK_CLOUDS = Lazy.of(() -> EnvironmentAttributeMap.builder().modify(EnvironmentAttributes.CLOUD_COLOR, ColorModifier.BLEND_TO_GRAY_ARGB, new ColorModifier.BlendToGray(0.095F, 0.94F)).build());

	Lazy<EnvironmentAttributeMap> TEST_SKYBOX = Lazy.of(() -> skybox(SkyboxTextureId.TEST).build());

	Map<String, Lazy<EnvironmentAttributeMap>> ALL = new HashMap<>(Map.ofEntries(
		Map.entry("day", DAY),
		Map.entry("night", NIGHT),
		Map.entry("storm", STORM),
		Map.entry("doom", DOOM),
		Map.entry("bright_night", BRIGHT_NIGHT),
		Map.entry("space", SPACE),
		Map.entry("black_screen", BLACK_SCREEN),
		Map.entry("white_screen", WHITE_SCREEN),
		Map.entry("green_screen", GREEN_SCREEN),
		Map.entry("blue_screen", BLUE_SCREEN),
		Map.entry("no_sun", NO_SUN),
		Map.entry("no_moon", NO_MOON),
		Map.entry("ash", ASH),
		Map.entry("dark_clouds", DARK_CLOUDS),
		Map.entry("test_skybox", TEST_SKYBOX)
	));

	Codec<EnvironmentAttributeMap> DIRECT_CODEC = KLibCodecs.or(Codec.STRING.flatXmap(string -> {
		var value = ALL.get(string);

		if (value != null) {
			return DataResult.success(value.get());
		}

		return KLibCodecErrors.error("Preset not found");
	}, map -> {
		for (var entry : ALL.entrySet()) {
			if (entry.getValue().get().equals(map)) {
				return DataResult.success(entry.getKey());
			}
		}

		return KLibCodecErrors.error("Preset not found");
	}), EnvironmentAttributeMap.CODEC);

	Codec<EnvironmentAttributeMap> CODEC = KLibCodecs.listOrSelf(DIRECT_CODEC).xmap(list -> {
		var builder = EnvironmentAttributeMap.builder();

		for (var map : list) {
			builder.putAll(map);
		}

		return builder.build();
	}, List::of);

	DataType<EnvironmentAttributeMap> DATA_TYPE = DataType.of(CODEC, ByteBufCodecs.fromCodecTrusted(CODEC));
}
