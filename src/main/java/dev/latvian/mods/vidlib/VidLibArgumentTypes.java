package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.codec.CodecArgument;
import dev.latvian.mods.vidlib.feature.registry.RefHolderArgument;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface VidLibArgumentTypes {
	@AutoRegister
	DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, VidLib.ID);

	DeferredHolder<ArgumentTypeInfo<?, ?>, CodecArgument.Info> CODEC_ARGUMENT = REGISTRY.register("codec", () -> ArgumentTypeInfos.registerByClass(Cast.to(CodecArgument.class), new CodecArgument.Info()));
	DeferredHolder<ArgumentTypeInfo<?, ?>, RefHolderArgument.Info> REF_HOLDER_ARGUMENT = REGISTRY.register("ref_holder", () -> ArgumentTypeInfos.registerByClass(Cast.to(RefHolderArgument.class), new RefHolderArgument.Info()));
}
