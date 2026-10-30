package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClientProps extends Props<ClientLevel> {
	public record PropWithState<P extends Prop, S extends PropRenderState<P>>(P prop, S state) {
		public S extractCore(PropExtractContext ctx) {
			state.extractCore(prop, ctx);
			return state;
		}

		public void extract(PropExtractContext ctx) {
			state.extract(prop, ctx);
		}

		public void submit(PropSubmitContext ctx) {
			state.submit(ctx);
		}
	}

	public static final ImBoolean VISIBLE = new ImBoolean(true);
	public static final IntSet OPEN_PROPS = new IntOpenHashSet();
	public static final IntSet HIDDEN_PROPS = new IntOpenHashSet();
	public static final Set<PropType<?>> HIDDEN_PROP_TYPES = new ReferenceOpenHashSet<>();
	public static final Visuals DEBUG_VISUALS = new Visuals();
	public static final ImBoolean HIDE_OUTLINE = new ImBoolean(true);

	public static boolean isPropHidden(Prop prop) {
		return HIDDEN_PROPS.contains(prop.id) || HIDDEN_PROP_TYPES.contains(prop.type);
	}

	@AutoInit(AutoInit.Type.CHUNKS_RENDERED)
	public static void chunksRendered(ClientLevel level) {
		level.getProps().reloadAll();
	}

	public final List<PropWithState<?, ?>> all;

	public ClientProps(ClientLevel level) {
		super(level);
		this.all = new LinkedList<>();
	}

	@Override
	protected void onAdded(Prop prop) {
		var factory = PropRenderState.BY_TYPE.get().get(prop.type);

		if (factory == null) {
			return;
		}

		var state = factory.create();

		if (state == null) {
			return;
		}

		all.add(new PropWithState(prop, state));
	}

	public void reloadAll() {
		all.clear();

		for (var list : propLists.values()) {
			for (var prop : list) {
				onAdded(prop);
			}
		}
	}

	@Override
	public void tick(boolean tick) {
		if (ReplayProp.MAP != null && ReplayProp.LIST != null) {
			var now = level.getGameTime();

			for (var existing : levelProps) {
				if (existing.clientSideOnly) {
					continue;
				}

				var p = ReplayProp.MAP.get(existing.id);

				if (p == null || !p.exists(now)) {
					existing.remove(PropRemoveType.TIME_TRAVEL);
				}
			}

			for (var p : ReplayProp.LIST) {
				var existing = levelProps.get(p.id);

				if (p.exists(now)) {
					if (existing == null) {
						create(context(p.type, PropSpawnType.GAME, p.spawn), true, true, null, null, prop -> {
							prop.id = p.id;

							for (var entry : p.data.entrySet()) {
								prop.setData(entry.getKey(), Cast.to(entry.getValue()));
							}

							prop.tick = (int) (now - p.spawn);
						});
					}
				} else if (existing != null) {
					existing.remove(PropRemoveType.TIME_TRAVEL);
				}
			}
		}

		super.tick(tick);

		if (ReplayProp.MAP != null && ReplayProp.LIST != null && !tick) {
			levelProps.map.values().removeIf(prop -> prop.removed == PropRemoveType.TIME_TRAVEL);
		}

		all.removeIf(pws -> pws.prop.isRemoved());
	}

	@Nullable
	public List<PropRenderState<?>> extractAll(PropExtractContext ctx) {
		if (all.isEmpty() || !VISIBLE.get()) {
			return null;
		}

		List<PropRenderState<?>> states = null;
		List<PropRenderState<?>> sortedStates = null;

		for (var propWithState : all) {
			var prop = propWithState.prop();

			if (prop.isRemoved()) {
				continue;
			} else if (prop.isTimeTraveling(ctx.gameTime())) {
				continue;
			} else if (isPropHidden(prop)) {
				continue;
			}

			var state = propWithState.extractCore(ctx);

			if (prop.isVisible(state.x, state.y, state.z, ctx, ctx.cameraPos(), state.squaredDistanceToCamera)) {
				propWithState.extract(ctx);

				if (state.invisible) {
					continue;
				}

				if (state.shouldSort) {
					if (sortedStates == null) {
						sortedStates = new ArrayList<>();
					}

					sortedStates.add(state);
				} else {
					if (states == null) {
						states = new ArrayList<>(all.size());
					}

					states.add(state);
				}
			}
		}

		// Might not be required anymore

		if (sortedStates != null) {
			if (sortedStates.size() >= 2) {
				sortedStates.sort(PropRenderState.COMPARATOR);
			}

			if (states == null) {
				states = sortedStates;
			} else {
				states.addAll(sortedStates);
			}
		}

		return states;
	}

	/*
	public void renderDebug(FrameInfo frame) {
		var ms = frame.poseStack();
		var cam = frame.camera().position();
		var delta = frame.worldDelta();

		for (var list : propLists.values()) {
			for (var prop : list) {
				double x = KMath.lerp(delta, prop.prevPos.x, prop.pos.x);
				double y = KMath.lerp(delta, prop.prevPos.y, prop.pos.y);
				double z = KMath.lerp(delta, prop.prevPos.z, prop.pos.z);
				double cd = cam.distanceToSqr(x, y + prop.height / 2D, z);

				if (prop.isVisible(x, y, z, frame, cam, cd)) {
					boolean selected = !HIDE_OUTLINE.get() && OPEN_PROPS.contains(prop.id);

					if (selected || frame.mc().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES)) {
						var progress = prop.getDebugVisualsProgress(delta);

						if (progress >= 0F && progress <= 1F) {
							prop.debugVisuals(Visuals.TEMP, x, y, z, delta, selected);
							MiscClientUtils.renderVisuals(ms, cam, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Visuals.TEMP, progress);
							Visuals.TEMP.clear();
						} else {
							prop.debugVisuals(DEBUG_VISUALS, x, y, z, delta, selected);
						}
					}
				}
			}
		}

		MiscClientUtils.renderVisuals(ms, cam, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, DEBUG_VISUALS, 1F);
		DEBUG_VISUALS.clear();
	}
	 */
}
