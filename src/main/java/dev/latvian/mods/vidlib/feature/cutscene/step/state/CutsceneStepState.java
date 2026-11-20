package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStepType;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CutsceneStepState {
	public record Factory<Step extends CutsceneStepType>(DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type, Function<Step, CutsceneStepState> factory, Supplier<CutsceneStepState> defaultFactory) {
	}

	public static final Lazy<Map<DynamicType<RegistryFriendlyByteBuf, CutsceneStepType>, Factory<?>>> ALL = Lazy.identityMap(map -> {
		for (var data : ClientAutoRegister.SCANNED.get()) {
			if (data.value() instanceof Factory<?> factory) {
				map.put(factory.type, factory);
			}
		}
	});

	public float start = 0F;
	public float length = 0F;
	public boolean snap = true;

	public void start(CutsceneState state, KNumberContext ctx) {
	}

	public void tick(CutsceneState state, KNumberContext ctx) {
	}

	public void exit(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);
	}
}
