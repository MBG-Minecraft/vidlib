package dev.latvian.mods.vidlib.feature.gradient;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@AutoInit
public interface GradientCommand {
	List<ResourceLocation> GRADIENT_IDS = new ArrayList<>();
	SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(VidLib.id("gradient"), () -> GRADIENT_IDS);
}
