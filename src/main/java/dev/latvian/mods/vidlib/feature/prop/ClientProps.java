package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClientProps extends Props<ClientLevel> {
	public record PropWithState<P extends Prop, S extends PropRenderState<P>>(P prop, S state) {
		public S extractCore(VidLibExtractContext ctx) {
			state.extractCore(prop, ctx);
			return state;
		}

		public void extract(VidLibExtractContext ctx) {
			state.extract(prop, ctx);
		}

		public void submit(SubmitContext ctx) {
			state.submit(ctx);
		}
	}

	public static final ImBoolean VISIBLE = new ImBoolean(true);
	public static final IntSet OPEN_PROPS = new IntOpenHashSet();
	public static final IntSet HIDDEN_PROPS = new IntOpenHashSet();
	public static final Set<PropType<?>> HIDDEN_PROP_TYPES = new ReferenceOpenHashSet<>();
	public static final ImBoolean HIDE_OUTLINE = new ImBoolean(true);

	public static boolean isPropHidden(Prop prop) {
		return HIDDEN_PROPS.contains(prop.id) || HIDDEN_PROP_TYPES.contains(prop.type);
	}

	@AutoInit(AutoInit.Type.CHUNKS_RELOADED)
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
	public List<PropRenderState<?>> extractAll(VidLibExtractContext ctx) {
		if (all.isEmpty() || !VISIBLE.get()) {
			return null;
		}

		List<PropRenderState<?>> states = null;
		List<PropRenderState<?>> sortedStates = null;

		boolean renderDebug = ctx.mc().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) || !ClientProps.OPEN_PROPS.isEmpty();

		for (var entry : all) {
			var prop = entry.prop;

			if (prop.isRemoved()) {
				continue;
			} else if (prop.isTimeTraveling(ctx.gameTime())) {
				continue;
			} else if (isPropHidden(prop)) {
				continue;
			}

			var state = entry.extractCore(ctx);

			if (prop.isVisible(state.x, state.y, state.z, ctx, ctx.cameraPos(), state.squaredDistanceToCamera)) {
				entry.extract(ctx);

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

				if (renderDebug) {
					boolean selected = !HIDE_OUTLINE.get() && OPEN_PROPS.contains(prop.id);
					prop.debugVisuals(ctx.vl().visuals, state.x, state.y, state.z, ctx.delta(), selected);
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
}
