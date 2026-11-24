package dev.latvian.mods.vidlib.feature.atmosphere;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;

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
	Lazy<EnvironmentAttributeMap> DAY_WITH_CELESTIALS = Lazy.of(() -> gradient(0x72B2FD, 0xB4D1FE).build());
	Lazy<EnvironmentAttributeMap> NIGHT = Lazy.of(() -> gradient(0x00001A, 0x123A70).build());
	Lazy<EnvironmentAttributeMap> NIGHT_WITH_CELESTIALS = Lazy.of(() -> gradient(0x00001A, 0x123A70).build());
	Lazy<EnvironmentAttributeMap> STORM = Lazy.of(() -> gradient(0x333A4C, 0x6E7C8A).build());
	Lazy<EnvironmentAttributeMap> DOOM = Lazy.of(() -> gradient(0x554C47, 0xA75A3D).build());
	Lazy<EnvironmentAttributeMap> BRIGHT_NIGHT = Lazy.of(() -> gradient(0x1C6D9E, 0x3D91A9).build());
	Lazy<EnvironmentAttributeMap> SPACE = Lazy.of(() -> gradient(0x00001A, 0x00001A).build());

	Lazy<EnvironmentAttributeMap> BLACK_SCREEN = Lazy.of(() -> screen(0x000000).build());
	Lazy<EnvironmentAttributeMap> WHITE_SCREEN = Lazy.of(() -> screen(0xFFFFFF).build());
	Lazy<EnvironmentAttributeMap> GREEN_SCREEN = Lazy.of(() -> screen(0x00FF00).build());
	Lazy<EnvironmentAttributeMap> BLUE_SCREEN = Lazy.of(() -> screen(0x0000FF).build());

	Lazy<EnvironmentAttributeMap> TEST_SKYBOX = Lazy.of(() -> skybox(SkyboxTextureId.TEST).build());

	Map<String, Lazy<EnvironmentAttributeMap>> ALL = Map.ofEntries(
		Map.entry("day", DAY),
		Map.entry("day_with_celestials", DAY_WITH_CELESTIALS),
		Map.entry("night", NIGHT),
		Map.entry("night_with_celestials", NIGHT_WITH_CELESTIALS),
		Map.entry("storm", STORM),
		Map.entry("doom", DOOM),
		Map.entry("bright_night", BRIGHT_NIGHT),
		Map.entry("space", SPACE),
		Map.entry("black_screen", BLACK_SCREEN),
		Map.entry("white_screen", WHITE_SCREEN),
		Map.entry("green_screen", GREEN_SCREEN),
		Map.entry("blue_screen", BLUE_SCREEN),
		Map.entry("test_skybox", TEST_SKYBOX)
	);
}
