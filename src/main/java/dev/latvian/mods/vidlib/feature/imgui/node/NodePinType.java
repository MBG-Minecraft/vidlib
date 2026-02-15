package dev.latvian.mods.vidlib.feature.imgui.node;

import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterImBuilder;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumberImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ParticleOptionsImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;
import net.minecraft.core.particles.ParticleOptions;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NodePinType<T> {
	public static final NodePinType<KNumber> NUMBER = new NodePinType<>("Number", KNumberImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<KVector> VECTOR = new NodePinType<>("Vector", KVectorImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<EntityFilter> ENTITY_FILTER = new NodePinType<>("Entity Filter", EntityFilterImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<BlockFilter> BLOCK_FILTER = new NodePinType<>("Block Filter", BlockFilterImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<ParticleOptions> PARTICLE_OPTIONS = new NodePinType<>("Particle Options", ParticleOptionsImBuilder.IMGUI_BUILDER_FACTORY);

	public static final NodePinType<Interpolation> INTERPOLATION = new NodePinType<>("Interpolation", NodePinShape.FILLED_SQUARE, editor -> MenuItem.root((graphics, menuItems) -> {
		// for (var easing : Easing.VALUES) {
		// 	menuItems.add(MenuItem.item(easing.name, g -> editor.accept(new ImBuilder.Unit<>(easing.name, easing).asNode())));
		// }
	}), null);

	public static final NodePinType<EntityNumber> ENTITY_NUMBER = new NodePinType<>("Entity Number", EntityNumberImBuilder.IMGUI_BUILDER_FACTORY);

	public final String displayName;
	public final NodePinShape defaultShape;
	public final List<NodePin> singleOutput;
	public final List<NodePin> singleRequiredInput;
	public final Function<Consumer<Node>, MenuItem> menu;
	public final Supplier<ImBuilder<T>> builderFactory;

	public NodePinType(String displayName, NodePinShape defaultShape, Function<Consumer<Node>, MenuItem> menu, Supplier<ImBuilder<T>> builderFactory) {
		this.displayName = displayName;
		this.defaultShape = defaultShape;
		this.singleOutput = List.of(output("Out"));
		this.singleRequiredInput = List.of(required("In"));
		this.menu = menu;
		this.builderFactory = builderFactory;
	}

	public NodePinType(String displayName, ImBuilderWrapper.Factory<T> factory) {
		this(displayName, NodePinShape.FILLED_TRIANGLE, editor -> MenuItem.root((graphics, menuItems) -> {
			int units = 0;

			for (var option : factory.getOptions()) {
				if (option.type() instanceof ImBuilderType.Unit) {
					units++;
				} else {
					menuItems.add(MenuItem.item(option.name(), g -> editor.accept(option.asNode())));
				}
			}

			if (units > 0) {
				menuItems.add(MenuItem.menu(ImIcon.NONE, "Other", (g1, list) -> {
					for (var option : factory.getOptions()) {
						if (option.type() instanceof ImBuilderType.Unit) {
							list.add(MenuItem.item(option.name(), g -> editor.accept(option.asNode())));
						}
					}
				}));
			}
		}), factory);
	}

	public NodePin output(String label) {
		return new NodePin(this, label, NodePinConnectionType.OUTPUT, defaultShape);
	}

	public NodePin required(String label) {
		return new NodePin(this, label, NodePinConnectionType.REQUIRED_INPUT, defaultShape);
	}

	public NodePin optional(String label) {
		return new NodePin(this, label, NodePinConnectionType.OPTIONAL_INPUT, defaultShape);
	}
}
