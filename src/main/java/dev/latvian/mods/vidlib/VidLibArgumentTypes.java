package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.codec.DataArgumentType;
import dev.latvian.mods.vidlib.feature.registry.RegistryOrDataArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface VidLibArgumentTypes {
	@AutoRegister
	DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, VidLib.ID);

	DeferredHolder<ArgumentTypeInfo<?, ?>, DataArgumentType.Info> DATA = REGISTRY.register("data", () -> ArgumentTypeInfos.registerByClass(Cast.to(DataArgumentType.class), new DataArgumentType.Info()));
	DeferredHolder<ArgumentTypeInfo<?, ?>, RegistryOrDataArgumentType.Info> REGISTRY_OR_DATA = REGISTRY.register("registry_or_data", () -> ArgumentTypeInfos.registerByClass(Cast.to(RegistryOrDataArgumentType.class), new RegistryOrDataArgumentType.Info()));
}
