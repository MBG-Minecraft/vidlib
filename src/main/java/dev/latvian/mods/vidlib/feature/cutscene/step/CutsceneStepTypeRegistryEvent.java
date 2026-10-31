package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CutsceneStepTypeRegistryEvent extends CustomRegistryTypeEvent<RegistryFriendlyByteBuf, NewCutsceneStepType> {
	public CutsceneStepTypeRegistryEvent(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, NewCutsceneStepType> registry) {
		super(registry);
	}
}
