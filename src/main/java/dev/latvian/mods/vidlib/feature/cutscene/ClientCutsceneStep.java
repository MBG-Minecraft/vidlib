package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.cutscene.event.CutsceneEvent;
import dev.latvian.mods.vidlib.feature.fade.Fade;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ClientCutsceneStep {
	public final int start;
	public final int length;
	public final int totalLength;
	public final WorldVector origin;
	public final WorldVector target;
	public final WorldNumber fovModifier;
	public final Component status;
	public final CutsceneStepBars bars;
	public final ResourceLocation shader;
	public final Fade fade;
	public final List<PositionedSoundData> sounds;
	public final CutsceneStepSnap snap;
	public final List<CutsceneEvent> events;
	public Vec3 prevRenderTarget;
	public Vec3 renderTarget;
	public List<CutsceneRender> render;

	public ClientCutsceneStep(CutsceneStep step, WorldNumberContext ctx) {
		this.start = step.start();
		this.length = Math.max(0, Mth.ceil(step.length().getOr(ctx, 0D)));
		this.totalLength = Math.max(0, start) + length;
		this.origin = step.origin().orElse(null);
		this.target = step.target().orElse(null);
		this.fovModifier = step.fovModifier().orElse(null);
		this.status = step.status().orElse(null);
		this.bars = step.bars().orElse(null);
		this.shader = step.shader().orElse(null);
		this.fade = step.fade().orElse(null);
		this.sounds = step.sounds();
		this.snap = step.snap();
		this.events = step.events();
	}
}
