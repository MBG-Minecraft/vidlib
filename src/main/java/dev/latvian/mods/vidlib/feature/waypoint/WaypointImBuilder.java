package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
import dev.latvian.mods.vidlib.feature.icon.EmptyIcon;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.CompoundImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.DimensionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.StringImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;

import java.util.ArrayList;
import java.util.List;

public class WaypointImBuilder extends CompoundImBuilder<Waypoint> {
	public static final ImBuilderType<Waypoint> TYPE = WaypointImBuilder::new;
	public static final ImBuilderType<List<Waypoint>> LIST_TYPE = () -> new ListImBuilder<>(TYPE);

	public final StringImBuilder id = new StringImBuilder();
	public final ImBuilder<EntityFilter> visible = EntityFilterImBuilder.IMGUI_BUILDER_FACTORY.get();
	public final DimensionImBuilder dimension = new DimensionImBuilder();
	public final ImBuilder<KVector> position = KVectorImBuilder.create();
	public final ImBuilder<Icon> icon = new IconImBuilder();
	public final FloatImBuilder alpha = new FloatImBuilder(0F, 255F);
	public final FloatImBuilder minDistance = new FloatImBuilder(0F, 500F);
	public final FloatImBuilder midDistance = new FloatImBuilder(0F, 500F);
	public final FloatImBuilder maxDistance = new FloatImBuilder(0F, 500F);
	public final TextComponentImBuilder label = new TextComponentImBuilder(false);
	public final BooleanImBuilder centered = new BooleanImBuilder();
	public final BooleanImBuilder showDistance = new BooleanImBuilder();
	public final BooleanImBuilder ignoreHeight = new BooleanImBuilder();

	public WaypointImBuilder() {
		visible.set(EntityFilter.ANY.instance());
		position.set(KVector.ZERO);
		icon.set(Waypoint.DEFAULT_ICON);
		alpha.set(255F);
		centered.set(true);
		showDistance.set(true);
		ignoreHeight.set(true);

		add("ID", id);
		add("Visible", visible);
		add("Dimension", dimension);
		add("Position", position);
		add("Icon", icon);
		add("Alpha", alpha);
		add("Min Distance", minDistance);
		add("Mid Distance", midDistance);
		add("Max Distance", maxDistance);
		add("Label", label);
		add("Centered", centered);
		add("Show Distance", showDistance);
		add("Ignore Height", ignoreHeight);
	}

	@Override
	public void set(Waypoint value) {
		if (value != null) {
			id.set(value.id());
			visible.set(value.visible());
			dimension.set(value.dimension());
			position.set(value.position());
			icon.set(value.icon());
			alpha.set(value.alpha());
			minDistance.set((float) value.minDistance());
			midDistance.set((float) value.midDistance());
			maxDistance.set((float) value.maxDistance());
			label.set(value.label());
			centered.set(value.centered());
			showDistance.set(value.showDistance());
			ignoreHeight.set(value.ignoreHeight());
		}
	}

	@Override
	public Waypoint build() {
		return new Waypoint(
			id.build(),
			visible.build(),
			dimension.build(),
			position.build(),
			icon.build(),
			alpha.build(),
			minDistance.build(),
			midDistance.build(),
			maxDistance.build(),
			label.build(),
			centered.build(),
			showDistance.build(),
			ignoreHeight.build()
		);
	}

	private static class IconImBuilder implements ImBuilder<Icon> {
		private static final List<Icon> COMMON_ICONS = List.of(
			Waypoint.DEFAULT_ICON,
			EmptyIcon.INSTANCE,
			Icon.YES.instance(),
			Icon.NO.instance()
		);

		private final Object[] selected = new Object[1];

		@Override
		public void set(Icon value) {
			selected[0] = value;
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var current = (Icon) selected[0];
			var options = new ArrayList<>(COMMON_ICONS);
			if (!COMMON_ICONS.contains(current)) {
				options.addFirst(current);
			}

			return graphics.combo("###icon", selected, "", options.toArray(), nameFunc -> {
				var i = (Icon) nameFunc;
				if (i == Waypoint.DEFAULT_ICON) {
					return "Default";
				}
				if (i instanceof EmptyIcon) {
					return "Empty";
				}
				if (i == Icon.YES.instance()) {
					return "Yes";
				}
				if (i == Icon.NO.instance()) {
					return "No";
				}
				return i.type().id();
			});
		}

		@Override
		public Icon build() {
			return (Icon) selected[0];
		}
	}
}
