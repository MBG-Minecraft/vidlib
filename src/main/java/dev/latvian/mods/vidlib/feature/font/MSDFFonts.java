package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceKey;

public interface MSDFFonts {
	ResourceKey<MSDFFont> JETBRAINS_MONO = MSDFFont.createKey(VidLib.id("jetbrains_mono"));
	ResourceKey<MSDFFont> KOMIKA_AXIS = MSDFFont.createKey(VidLib.id("komika_axis"));
	ResourceKey<MSDFFont> SAIRA_CONDENSED_BOLD = MSDFFont.createKey(VidLib.id("saira_condensed_bold"));
	ResourceKey<MSDFFont> SAIRA_CONDENSED_MEDIUM = MSDFFont.createKey(VidLib.id("saira_condensed_medium"));
	ResourceKey<MSDFFont> SAIRA_EXTRACONDENSED_EXTRABOLDITALIC = MSDFFont.createKey(VidLib.id("saira_extracondensed_extrabolditalic"));
	ResourceKey<MSDFFont> SAIRA_EXTRACONDENSED_ITALIC = MSDFFont.createKey(VidLib.id("saira_extracondensed_italic"));
}
