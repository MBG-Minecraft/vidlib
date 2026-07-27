package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.resources.ResourceKey;

public interface MSDFFonts {
	ResourceKey<MSDFFont> LAT = MSDFFont.createKey(ID.vidlib("lat"));
	ResourceKey<MSDFFont> JETBRAINS_MONO = MSDFFont.createKey(ID.vidlib("jetbrains_mono"));
	ResourceKey<MSDFFont> KOMIKA_AXIS = MSDFFont.createKey(ID.vidlib("komika_axis"));
	ResourceKey<MSDFFont> SAIRA_CONDENSED_BOLD = MSDFFont.createKey(ID.vidlib("saira_condensed_bold"));
	ResourceKey<MSDFFont> SAIRA_CONDENSED_MEDIUM = MSDFFont.createKey(ID.vidlib("saira_condensed_medium"));
	ResourceKey<MSDFFont> SAIRA_EXTRACONDENSED = MSDFFont.createKey(ID.vidlib("saira_extracondensed"));
	ResourceKey<MSDFFont> SAIRA_EXTRACONDENSED_EXTRABOLD = MSDFFont.createKey(ID.vidlib("saira_extracondensed_extrabold"));
	ResourceKey<MSDFFont> READY = MSDFFont.createKey(ID.vidlib("ready"));
}
