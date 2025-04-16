package dev.latvian.mods.vidlib.feature.cutscene.event;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
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
