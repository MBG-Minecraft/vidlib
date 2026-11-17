package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.entity.PositionType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.DepthOfFieldCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.AngleImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vector3dImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.integration.replay.SelectedPropReplaySessionData;
import dev.latvian.mods.vidlib.math.kvector.FollowingPropKVector;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import it.unimi.dsi.fastutil.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class PropUI<P extends Prop> {
	public record Factory<P extends Prop>(PropType<P> type, Function<P, PropUI<P>> factory) {
		public PropUI<P> create(P prop) {
			return factory.apply(prop);
		}
	}

	@FunctionalInterface
	public interface BuilderCollector {
		void add(Pair<PropData<?, ?>, ImBuilder<?>> pair);

		default <V> void add(PropData<?, V> data, ImBuilder<V> builder) {
			add(Pair.of(data, builder));
		}
	}

	public static final Lazy<Map<PropType<?>, Factory<?>>> ALL = Lazy.identityMap(map -> {
		for (var s : ClientAutoRegister.SCANNED.get()) {
			if (s.value() instanceof Factory<?> factory) {
				map.put(factory.type, factory);
			}
		}
	});

	public static PropUI<?> of(Prop prop) {
		if (prop.ui == null) {
			var factory = ALL.get().get(prop.type);
			prop.ui = factory == null ? new PropUI<>(prop) : factory.create(Cast.to(prop));
		}

		return (PropUI<?>) prop.ui;
	}

	public final P prop;
	private final List<Pair<PropData<?, ?>, ImBuilder<?>>> imguiBuilders;

	public PropUI(P prop) {
		this.prop = prop;
		this.imguiBuilders = new ArrayList<>();
	}

	public void collectBuilders(BuilderCollector collector) {
		if (prop.type.contains(Prop.POSITION)) {
			collector.add(Prop.POSITION, Vector3dImBuilder.TYPE.get());
		}

		if (prop.type.contains(Prop.YAW)) {
			collector.add(Prop.YAW, AngleImBuilder.TYPE_360.get());
		}

		if (prop.type.contains(Prop.PITCH)) {
			collector.add(Prop.PITCH, AngleImBuilder.TYPE_90.get());
		}
	}

	public void imgui(ImGraphics graphics, float delta) {
		graphics.pushStack();

		if (graphics.isReplay && !prop.clientSideOnly) {
			ImGui.beginDisabled();
		}

		graphics.setRedButton();

		if (ImGui.button(ImIcons.TRASHCAN + "###remove")) {
			graphics.mc.runClientCommand((prop.clientSideOnly ? "client-prop remove id " : "prop remove id ") + prop.getIdString());
		}

		graphics.hoveredTooltip("Remove");

		if (graphics.isReplay && !prop.clientSideOnly) {
			ImGui.endDisabled();
		}

		graphics.popStack();

		ImGui.sameLine();

		boolean isHidden = ClientProps.HIDDEN_PROPS.contains(prop.id);

		if (graphics.button((isHidden ? ImIcons.INVISIBLE : ImIcons.VISIBLE) + "###visible", isHidden ? ImColorVariant.RED : null)) {
			if (isHidden) {
				ClientProps.HIDDEN_PROPS.remove(prop.id);
			} else {
				ClientProps.HIDDEN_PROPS.add(prop.id);
			}
		}

		graphics.hoveredTooltip(isHidden ? "Hidden" : "Visible");

		ImGui.sameLine();

		boolean isTypeHidden = ClientProps.HIDDEN_PROP_TYPES.contains(prop.type);

		if (graphics.button((isTypeHidden ? ImIcons.INVISIBLE : ImIcons.VISIBLE) + ImIcons.ASTERIX.toString() + "###type-visible", isTypeHidden ? ImColorVariant.RED : null)) {
			if (isTypeHidden) {
				ClientProps.HIDDEN_PROP_TYPES.remove(prop.type);
			} else {
				ClientProps.HIDDEN_PROP_TYPES.add(prop.type);
			}
		}

		graphics.hoveredTooltip(isTypeHidden ? "Type Hidden" : "Type Visible");

		ImGui.sameLine();

		if (ReplayProp.LIST != null && !prop.clientSideOnly) {
			ImGui.beginDisabled();
		}

		if (ImGui.button(ImIcons.PASTE + "###clone")) {
			graphics.mc.runClientCommand((prop.clientSideOnly ? "client-prop clone " : "prop clone ") + prop.getIdString());
		}

		graphics.hoveredTooltip("Clone");

		if (ReplayProp.LIST != null && !prop.clientSideOnly) {
			ImGui.endDisabled();
		}

		if (DepthOfFieldCanvas.ENABLED.get()) {
			ImGui.sameLine();

			if (ImGui.button(ImIcons.APERTURE + "###focus-dof")) {
				DepthOfFieldCanvas.focusPosKVector = new FollowingPropKVector(prop.id, PositionType.EYES).ref();
			}

			graphics.hoveredTooltip("Focus DoF");
		}

		ImGui.sameLine();

		if (ImGui.button(ImIcons.DATABASE + "###copy-id")) {
			ImGui.setClipboardText(prop.getIdString());
		}

		graphics.hoveredTooltip("Copy ID");

		ImGui.sameLine();

		if (graphics.button(ImIcons.COPY + "###copy-nbt", ImColorVariant.LIME)) {
			ImGui.setClipboardText(prop.encode(graphics.nbtOps).toString());
		}

		graphics.hoveredTooltip("Copy NBT");

		ImGui.sameLine();

		if (graphics.smallButton(ImIcons.LOCATION + "###teleport", ImColorVariant.DARK_PURPLE)) {
			graphics.mc.runClientCommand("tp @s " + prop.pos.x + " " + prop.pos.y + " " + prop.pos.z);
		}

		graphics.hoveredTooltip("Teleport To");

		if (prop.clientSideOnly) {
			ImGui.sameLine();
			graphics.button(ImIcons.WARNING + "###client-only", ImColorVariant.ORANGE);
			graphics.hoveredTooltip("Client-Side Only!");
		}

		if (ImGui.beginTable("###data", 3, ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.Borders)) {
			if (prop.hasData(Prop.TICK)) {
				ImGui.tableNextRow();
				ImGui.pushID("tick");
				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (ImGui.smallButton(ImIcons.UNDO + "###reset")) {
					prop.prevTick = 0;
					prop.tick = 0;
					prop.c2sEdit(Prop.TICK, 0, true);
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (ImGui.checkbox("tick", !prop.paused)) {
					if (prop.paused) {
						graphics.mc.runClientCommand((prop.clientSideOnly ? "client-prop unpause " : "prop unpause ") + prop.getIdString());
					} else {
						graphics.mc.runClientCommand((prop.clientSideOnly ? "client-prop pause " : "prop pause ") + prop.getIdString());
					}
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				int lifespan1 = prop.lifespan > 0 && prop.hasData(Prop.LIFESPAN) ? prop.lifespan : 0;
				var tickText = lifespan1 > 0 ? "%,d / %,d".formatted(prop.tick, lifespan1) : "%,d".formatted(prop.tick);

				if (prop.paused) {
					ImGuiUtils.INT.set(prop.tick);
					ImGui.pushItemWidth(-1F);
					ImGui.dragInt("###value", ImGuiUtils.INT.getData(), 1F, 0, 1000000, tickText);
					ImGui.popItemWidth();

					var update = ImUpdate.itemEdit();

					if (update.isAny()) {
						prop.c2sEdit(Prop.TICK, Math.max(0, ImGuiUtils.INT.get()), update.isFull());
					}
				} else {
					ImGui.text(tickText);
				}

				ImGui.popID();

				if (lifespan1 > 0) {
					ImGui.tableNextRow();
					ImGui.pushID("progress");
					ImGui.tableNextColumn();
					ImGui.beginDisabled();
					ImGui.alignTextToFramePadding();
					ImGui.smallButton(ImIcons.UNDO + "###reset");
					ImGui.endDisabled();
					ImGui.tableNextColumn();
					ImGui.alignTextToFramePadding();
					ImGui.text("progress");
					ImGui.tableNextColumn();
					ImGui.progressBar(prop.getRelativeTick(delta, 0F), 0F, 20F);
					ImGui.popID();
				}
			}

			imguiBuilders.clear();
			collectBuilders(imguiBuilders::add);

			for (var entry : imguiBuilders) {
				var data = entry.left();
				var builder = entry.right();

				ImGui.tableNextRow();
				ImGui.pushID(data.key());
				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				var value = prop.getData(data);

				try {
					builder.set(Cast.to(value));
				} catch (Throwable ex) {
					graphics.stackTrace(ex);
				}

				var defaultValue = prop.defaultValues.get(data);
				var isDefault = Objects.equals(defaultValue, value);

				if (isDefault) {
					ImGui.beginDisabled();
				}

				if (ImGui.smallButton(ImIcons.UNDO + "###reset")) {
					prop.c2sEdit(data, Cast.to(defaultValue), true);
				}

				if (!isDefault && ImGui.isItemHovered()) {
					graphics.tooltip("Reset to " + defaultValue);
				}

				if (isDefault) {
					ImGui.endDisabled();
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();
				graphics.redTextIf(data.key(), !builder.isValid());

				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);
				ImGui.pushID("###value");
				var update = builder.imgui(graphics);
				ImGui.popID();
				ImGui.popItemWidth();

				if (update.isAny() && builder.isValid()) {
					var selectedPropData = ReplayAPI.getActiveSessionData(SelectedPropReplaySessionData.TYPE);

					if (selectedPropData != null) {
						selectedPropData.makePropKeyframes.add(prop);
					}

					prop.c2sEdit(data, Cast.to(builder.build()), update.isFull());
				}

				ImGui.popID();
			}

			ImGui.endTable();
		}
	}
}
