package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface NewCutsceneStepType extends CustomRegistryValue<RegistryFriendlyByteBuf, NewCutsceneStepType> {
	CustomRegistry<RegistryFriendlyByteBuf, NewCutsceneStepType> REGISTRY = CustomRegistry.create("cutscene_step");

	Codec<Ref<NewCutsceneStepType>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<NewCutsceneStepType>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<NewCutsceneStepType>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, NewCutsceneStepType> registry) {
		// registry.register(LemniscateScreenShakeType.TYPE);
	}

	@Override
	default CustomRegistry<RegistryFriendlyByteBuf, NewCutsceneStepType> getRegistry() {
		return REGISTRY;
	}

	// String name,
	// ImIcon icon,
	// String displayName,
	// boolean hasSnap,
	// Supplier<T> factory,
	// Codec<T> codec,
	// StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
}
