package dev.beast.mods.shimmer.feature.cutscene.event;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public interface CutsceneEvent {
	SimpleRegistry<CutsceneEvent> REGISTRY = SimpleRegistry.create(CutsceneEvent::type);
	Codec<CutsceneEvent> CODEC = Codec.either(Codec.STRING, CutsceneEvent.REGISTRY.valueCodec()).xmap(either -> either.map(CustomCutsceneEventHolder::new, Function.identity()), event -> event instanceof CustomCutsceneEventHolder(String name) ? Either.left(name) : Either.right(event));

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(CustomCutsceneEventHolder.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	void run(Level level, WorldNumberContext ctx);
}
