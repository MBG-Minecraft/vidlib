package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.sound.StopSoundTask;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;

public class SoundCutsceneStep extends CutsceneStep {
	public static final Codec<SoundCutsceneStep> CODEC = PositionedSoundData.CODEC.xmap(SoundCutsceneStep::new, s -> s.sound);

	public static final StreamCodec<RegistryFriendlyByteBuf, SoundCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		PositionedSoundData.STREAM_CODEC, o -> o.sound,
		SoundCutsceneStep::new
	);

	public PositionedSoundData sound;

	public SoundCutsceneStep(PositionedSoundData sound) {
		this.sound = sound;
	}

	public SoundCutsceneStep() {
		this.sound = new PositionedSoundData(new SoundData(SoundEvents.EXPERIENCE_ORB_PICKUP));
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.SOUND;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var mc = Minecraft.getInstance();
		var instance = ClientSessionData.of(mc).createSoundInstance(sound, ctx);

		if (sound.stopImmediately()) {
			state.exitTasks.add(new StopSoundTask(this, instance));
		}

		mc.getSoundManager().play(instance);
	}

	@Override
	public void exit(CutsceneState state, KNumberContext ctx) {
		if (sound.stopImmediately()) {
			var itr = state.exitTasks.iterator();

			while (itr.hasNext()) {
				if (itr.next() instanceof StopSoundTask t && t.parent() == this) {
					t.run();
					itr.remove();
					return;
				}
			}
		}
	}
}
