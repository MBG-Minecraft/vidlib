package dev.latvian.mods.vidlib.feature.cutscene.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

public record CustomCutsceneEventHolder(String name) implements CutsceneEvent {
	public static final SimpleRegistryType<CustomCutsceneEventHolder> TYPE = SimpleRegistryType.dynamic(VidLib.id("custom"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(CustomCutsceneEventHolder::name)
	).apply(instance, CustomCutsceneEventHolder::new)), ByteBufCodecs.STRING_UTF8.map(CustomCutsceneEventHolder::new, CustomCutsceneEventHolder::name));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public void run(Level level, WorldNumberContext ctx) {
		NeoForge.EVENT_BUS.post(new CustomCutsceneEvent(level, ctx, name));
	}
}
