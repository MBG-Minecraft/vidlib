package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface CutsceneStepType extends CustomRegistryValue<RegistryFriendlyByteBuf, CutsceneStepType> {
	CustomRegistry<RegistryFriendlyByteBuf, CutsceneStepType> REGISTRY = CustomRegistry.create("cutscene_step");

	Codec<Ref<CutsceneStepType>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<CutsceneStepType>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<CutsceneStepType>> DATA_TYPE = REGISTRY.dataType();

	int STATE_FACTORY = DynamicType.newPropertyId();

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, CutsceneStepType> registry) {
		// registry.register(LemniscateScreenShakeType.TYPE);

		registry.register(OriginCutsceneStep.TYPE);
		registry.register(TargetCutsceneStep.TYPE);
		registry.register(FOVModifierCutsceneStep.TYPE);
		registry.register(StatusTextCutsceneStep.TYPE);
		registry.register(BarVisibilityCutsceneStep.TYPE);
		registry.register(TopBarTextCutsceneStep.TYPE);
		registry.register(BottomBarTextCutsceneStep.TYPE);
		registry.register(ScreenEffectCutsceneStep.TYPE);
		registry.register(SoundCutsceneStep.TYPE);
		registry.register(CustomEventCutsceneStep.TYPE);
	}

	@Override
	default CustomRegistry<RegistryFriendlyByteBuf, CutsceneStepType> getRegistry() {
		return REGISTRY;
	}

	String getDisplayName();

	ImIcon getIcon();

	default boolean getHasSnap() {
		return false;
	}
}
