package dev.latvian.mods.vidlib.feature.gradient;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.kmath.color.GradientReference;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.commands.CommandSourceStack;

@AutoInit
public interface GradientCommand {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("gradient"), () -> GradientReference.MAP.keySet());
}
