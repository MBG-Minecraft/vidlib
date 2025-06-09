package dev.latvian.mods.vidlib.feature.gradient;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.klib.color.GradientReference;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.commands.CommandSourceStack;

@AutoInit
public interface GradientCommand {
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("gradient"), () -> GradientReference.MAP.keySet());
}
