package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.npc.NPCParticle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public interface VidLibClientParticles {
	static void register(RegisterParticleProvidersEvent event) {
		event.registerSpecial(VidLibParticles.SHAPE.get(), (options, level, x, y, z, xd, yd, zd, random) -> new ShapeParticle((ShapeParticleOptions) options, level, x, y, z, xd, yd, zd));
		event.registerSpecial(VidLibParticles.LINE.get(), (options, level, x, y, z, xd, yd, zd, random) -> new LineParticle((LineParticleOptions) options, level, x, y, z, xd, yd, zd));
		event.registerSpecial(VidLibParticles.TEXT.get(), (options, level, x, y, z, xd, yd, zd, random) -> new TextParticle((TextParticleOptions) options, level, x, y, z, xd, yd, zd));
		event.registerSpecial(VidLibParticles.ITEM.get(), (options, level, x, y, z, xd, yd, zd, random) -> new ItemParticle((ItemParticleOptions) options, level, x, y, z, xd, yd, zd));
		event.registerSpecial(VidLibParticles.NPC.get(), (options, level, x, y, z, xd, yd, zd, random) -> new NPCParticle((dev.latvian.mods.vidlib.feature.npc.NPCParticleOptions) options, level, x, y, z, xd, yd, zd));
		event.registerSpecial(VidLibParticles.LIGHTNING.get(), (options, level, x, y, z, xd, yd, zd, random) -> new LightningParticle((LightningParticleOptions) options, level, x, y, z, xd, yd, zd));
		event.registerSpriteSet(VidLibParticles.BURN_SMOKE.get(), BurnSmokeParticle::create);
		event.registerSpriteSet(VidLibParticles.SPARK.get(), SparkParticle::create);
		event.registerSpriteSet(VidLibParticles.WIND.get(), WindParticle::create);
		event.registerSpriteSet(VidLibParticles.FIRE.get(), FireParticle::create);
	}
}
