package dev.latvian.mods.vidlib.feature.cutscene;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CutsceneState {
	public Vec3 prevOrigin, origin;
	public Vec3 prevTarget, target;
	public double prevFovMod, fovMod;
	public float prevBarVisibility, barVisibility;
	public final List<Component> topBar;
	public final List<Component> bottomBar;
	public final List<Runnable> exitTasks;

	public CutsceneState() {
		this.origin = null;
		this.target = null;
		this.fovMod = 1D;
		this.barVisibility = 0F;
		this.topBar = new ArrayList<>();
		this.bottomBar = new ArrayList<>();
		this.exitTasks = new ArrayList<>();
	}

	public void snap() {
		prevOrigin = origin;
		prevTarget = target;
		prevFovMod = fovMod;
		prevBarVisibility = barVisibility;
	}
}
