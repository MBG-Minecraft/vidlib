package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CutsceneStepTypeRegistryEvent extends CustomRegistryTypeEvent<RegistryFriendlyByteBuf, CutsceneStepType> {
	public CutsceneStepTypeRegistryEvent(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, CutsceneStepType> registry) {
		super(registry);
	}
}
