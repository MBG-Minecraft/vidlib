package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.codec.CodecArgument;
import dev.beast.mods.shimmer.feature.codec.OptionalCodecArgument;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ShimmerArgumentTypes {
	@AutoRegister
	DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Shimmer.ID);

	DeferredHolder<ArgumentTypeInfo<?, ?>, CodecArgument.Info> CODEC_ARGUMENT = REGISTRY.register("codec", () -> ArgumentTypeInfos.registerByClass(Cast.to(CodecArgument.class), new CodecArgument.Info()));
	DeferredHolder<ArgumentTypeInfo<?, ?>, OptionalCodecArgument.Info> OPTIONAL_CODEC_ARGUMENT = REGISTRY.register("optional_codec", () -> ArgumentTypeInfos.registerByClass(Cast.to(OptionalCodecArgument.class), new OptionalCodecArgument.Info()));
}
