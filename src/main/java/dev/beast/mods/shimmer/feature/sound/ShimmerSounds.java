package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ShimmerSounds {
	@AutoRegister
	DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, Shimmer.ID);

	private static DeferredHolder<SoundEvent, SoundEvent> register(String name, @Nullable Float fixedRange) {
		return REGISTRY.register(name, () -> new SoundEvent(Shimmer.id(name), Optional.ofNullable(fixedRange)));
	}

	DeferredHolder<SoundEvent, SoundEvent> SPLAT = register("splat", null);
	DeferredHolder<SoundEvent, SoundEvent> WOOSH = register("woosh", null);
}
