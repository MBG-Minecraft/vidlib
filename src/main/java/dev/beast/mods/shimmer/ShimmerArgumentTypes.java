package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.codec.CodecArgument;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.registry.RefHolderArgument;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import dev.beast.mods.shimmer.util.registry.VideoResourceLocationArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ShimmerArgumentTypes {
	@AutoRegister
	DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Shimmer.ID);

	DeferredHolder<ArgumentTypeInfo<?, ?>, CodecArgument.Info> CODEC_ARGUMENT = REGISTRY.register("codec", () -> ArgumentTypeInfos.registerByClass(Cast.to(CodecArgument.class), new CodecArgument.Info()));
	DeferredHolder<ArgumentTypeInfo<?, ?>, RefHolderArgument.Info> REF_HOLDER_ARGUMENT = REGISTRY.register("ref_holder", () -> ArgumentTypeInfos.registerByClass(Cast.to(RefHolderArgument.class), new RefHolderArgument.Info()));
	DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ShimmerResourceLocationArgument>> SHIMMER_RESOURCE_LOCATION = REGISTRY.register("shimmer_resource_location", () -> ArgumentTypeInfos.registerByClass(ShimmerResourceLocationArgument.class, SingletonArgumentInfo.contextFree(ShimmerResourceLocationArgument::id)));
	DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<VideoResourceLocationArgument>> VIDEO_RESOURCE_LOCATION = REGISTRY.register("video_resource_location", () -> ArgumentTypeInfos.registerByClass(VideoResourceLocationArgument.class, SingletonArgumentInfo.contextFree(VideoResourceLocationArgument::id)));
}
