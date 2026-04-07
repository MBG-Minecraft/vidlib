package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceKey;

public interface MSDFFonts {
	ResourceKey<MSDFFont> LAT = MSDFFont.createKey(VidLib.id("lat"));
	ResourceKey<MSDFFont> JETBRAINS_MONO = MSDFFont.createKey(VidLib.id("jetbrains_mono"));
	ResourceKey<MSDFFont> KOMIKA_AXIS = MSDFFont.createKey(VidLib.id("komika_axis"));
	ResourceKey<MSDFFont> SAIRA_CONDENSED_BOLD = MSDFFont.createKey(VidLib.id("saira_condensed_bold"));
	ResourceKey<MSDFFont> SAIRA_CONDENSED_MEDIUM = MSDFFont.createKey(VidLib.id("saira_condensed_medium"));
	ResourceKey<MSDFFont> SAIRA_EXTRACONDENSED = MSDFFont.createKey(VidLib.id("saira_extracondensed"));
	ResourceKey<MSDFFont> SAIRA_EXTRACONDENSED_EXTRABOLD = MSDFFont.createKey(VidLib.id("saira_extracondensed_extrabold"));
	ResourceKey<MSDFFont> READY = MSDFFont.createKey(VidLib.id("ready"));
}
