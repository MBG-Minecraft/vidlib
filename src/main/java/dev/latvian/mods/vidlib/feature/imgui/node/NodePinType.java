package dev.latvian.mods.vidlib.feature.imgui.node;

import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterImBuilder;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilder;
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

public class NodePinType<T> {
	public static final NodePinType<KNumber> NUMBER = new NodePinType<>("Number", KNumberImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<KVector> VECTOR = new NodePinType<>("Vector", KVectorImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<EntityFilter> ENTITY_FILTER = new NodePinType<>("Entity Filter", EntityFilterImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<BlockFilter> BLOCK_FILTER = new NodePinType<>("Block Filter", BlockFilterImBuilder.IMGUI_BUILDER_FACTORY);
	public static final NodePinType<ParticleOptions> PARTICLE_OPTIONS = new NodePinType<>("Particle Options", ParticleOptionsImBuilder.IMGUI_BUILDER_FACTORY);

	public static final NodePinType<Easing> EASING = new NodePinType<>("Easing", editor -> MenuItem.root((graphics, menuItems) -> {
		for (var easing : Easing.VALUES) {
			menuItems.add(MenuItem.item(easing.name, g -> editor.accept(new ImBuilder.Unit<>(easing.name, easing).asNode())));
		}
	}));

	public final String displayName;
	public final List<NodePin> singleOutput;
	public final List<NodePin> singleRequiredInput;
	public final Function<Consumer<Node>, MenuItem> menu;

	public NodePinType(String displayName, Function<Consumer<Node>, MenuItem> menu) {
		this.displayName = displayName;
		this.singleOutput = List.of(output("Out"));
		this.singleRequiredInput = List.of(required("In"));
		this.menu = menu;
	}

	public NodePinType(String displayName, ImBuilderWrapper.Factory<T> factory) {
		this(displayName, editor -> MenuItem.root((graphics, menuItems) -> {
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
		}));
	}

	public NodePin output(String label) {
		return new NodePin(this, label, NodePinConnectionType.OUTPUT);
	}

	public NodePin required(String label) {
		return new NodePin(this, label, NodePinConnectionType.REQUIRED_INPUT);
	}

	public NodePin optional(String label) {
		return new NodePin(this, label, NodePinConnectionType.OPTIONAL_INPUT);
	}
}
