package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface VidLibSounds {
	@AutoRegister
	DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, VidLib.ID);

	private static DeferredHolder<SoundEvent, SoundEvent> register(String name, @Nullable Float fixedRange) {
		return REGISTRY.register(name, () -> new SoundEvent(VidLib.id(name), Optional.ofNullable(fixedRange)));
	}

	DeferredHolder<SoundEvent, SoundEvent> SPLAT = register("splat", null);
	DeferredHolder<SoundEvent, SoundEvent> WOOSH = register("woosh", null);
	DeferredHolder<SoundEvent, SoundEvent> FAR_WOOSH = register("far_woosh", null);
	DeferredHolder<SoundEvent, SoundEvent> FAR_EXPLOSION = register("far_explosion", null);
}
