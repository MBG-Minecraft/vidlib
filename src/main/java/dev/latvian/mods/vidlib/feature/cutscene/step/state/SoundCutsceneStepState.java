package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.SoundCutsceneStep;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.sound.StopSoundTask;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

public class SoundCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<SoundCutsceneStep> FACTORY = new Factory<>(SoundCutsceneStep.TYPE, SoundCutsceneStepState::new, SoundCutsceneStepState::new);

	public PositionedSoundData sound;

	public SoundCutsceneStepState(SoundCutsceneStep step) {
		this.sound = step.sound();
	}

	public SoundCutsceneStepState() {
		this.sound = new PositionedSoundData(new SoundData(SoundEvents.EXPERIENCE_ORB_PICKUP));
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
