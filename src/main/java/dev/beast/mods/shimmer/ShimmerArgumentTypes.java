package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.misc.CodecArgument;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ShimmerArgumentTypes {
	DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Shimmer.ID);

	DeferredHolder<ArgumentTypeInfo<?, ?>, CodecArgument.Info> CODEC_ARGUMENT = REGISTRY.register("codec", () -> ArgumentTypeInfos.registerByClass(Cast.to(CodecArgument.class), new CodecArgument.Info()));
}
