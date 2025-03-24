package dev.beast.mods.shimmer.feature.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.feature.misc.ShimmerIcons;
import dev.beast.mods.shimmer.math.Range;
import dev.beast.mods.shimmer.util.JsonUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigScreen<C> extends Screen {
	public static abstract class ConfigContainer<C, T> {
		public final DynamicOps<JsonElement> ops;
		public final C instance;
		public final ConfigValue<C, T> config;

		public ConfigContainer(DynamicOps<JsonElement> ops, C instance, ConfigValue<C, T> config) {
			this.ops = ops;
			this.instance = instance;
			this.config = config;
		}

		public String encode() {
			return config.encode(ops, instance).toString();
		}

		public abstract void addWidget(ConfigScreen<C> screen, int x, int y, int w, int h);
	}

	public static class ConfigEditBox<C, T> extends ConfigContainer<C, T> implements Consumer<String> {
		public EditBox editBox;

		public ConfigEditBox(DynamicOps<JsonElement> ops, C instance, ConfigValue<C, T> config) {
			super(ops, instance, config);
		}

		@Override
		public void addWidget(ConfigScreen<C> screen, int x, int y, int w, int h) {
			boolean fresh = editBox == null;
			editBox = new EditBox(screen.font, x, y, w, h, editBox, Component.empty());
			editBox.setResponder(this);

			screen.addRenderableWidget(editBox);

			if (fresh) {
				editBox.setValue(encode());
			}
		}

		@Override
		public void accept(String s) {
			try {
				var tag = JsonUtils.GSON.fromJson(s, JsonElement.class);
				config.decode(ops, instance, tag);
				editBox.setTextColor(0xFFFFFFFF);
			} catch (Exception ex) {
				editBox.setTextColor(0xFFFF0000);
			}
		}
	}

	public static class ConfigToggleButton<C> extends ConfigContainer<C, Boolean> {
		public Button button;

		public ConfigToggleButton(DynamicOps<JsonElement> ops, C instance, BooleanConfigValue<C> config) {
			super(ops, instance, config);
		}

		@Override
		public void addWidget(ConfigScreen<C> screen, int x, int y, int w, int h) {
			button = new Button.Builder(config.valueComponent(ops, config.getter.apply(instance)), b -> {
				boolean v = !config.getter.apply(instance);
				config.setter.accept(instance, v);
				button.setMessage(config.valueComponent(ops, v));
			}).bounds(x, y, w, h).build();

			screen.addRenderableWidget(button);
		}
	}

	public static class ConfigFloatSlider<C> extends ConfigContainer<C, Float> {
		public AbstractSliderButton slider;
		public final Range range;

		public ConfigFloatSlider(DynamicOps<JsonElement> ops, C instance, FloatConfigValue<C> config) {
			super(ops, instance, config);
			this.range = config.range;
		}

		@Override
		public void addWidget(ConfigScreen<C> screen, int x, int y, int w, int h) {
			var value = config.getter.apply(instance);
			slider = new AbstractSliderButton(x, y, w, h, config.valueComponent(ops, value), range.delta(value)) {
				@Override
				protected void updateMessage() {
					setMessage(config.valueComponent(ops, range.get((float) value)));
				}

				@Override
				protected void applyValue() {
					config.setter.accept(instance, range.get((float) value));
				}
			};

			screen.addRenderableWidget(slider);
		}
	}

	public final C instance;
	public final C defaultInstance;
	public final List<ConfigContainer<C, ?>> configWidgets;
	public final Consumer<C> update;

	public ConfigScreen(DynamicOps<JsonElement> ops, C instance, C defaultInstance, List<ConfigValue<C, ?>> config, Consumer<C> update) {
		super(Component.empty());
		this.instance = instance;
		this.defaultInstance = defaultInstance;
		this.configWidgets = new ArrayList<>(config.size());
		this.update = update;

		for (var value : config) {
			if (value instanceof BooleanConfigValue b) {
				configWidgets.add(new ConfigToggleButton<>(ops, instance, b));
			} else if (value instanceof FloatConfigValue b && b.slider && b.range != null) {
				configWidgets.add(new ConfigFloatSlider<>(ops, instance, b));
			} else {
				configWidgets.add(new ConfigEditBox<>(ops, instance, value));
			}
		}
	}

	public void addCopyJsonButton(Codec<C> fullCodec) {
		addRenderableWidget(Button.builder(Component.empty().append(ShimmerIcons.icons(ShimmerIcons.COPY + ShimmerIcons.SMALL_SPACE)).append("Copy"), button -> {
			try {
				minecraft.keyboardHandler.setClipboard(JsonUtils.GSON.toJson(fullCodec.encodeStart(minecraft.level.registryAccess().createSerializationContext(JsonOps.INSTANCE), instance).getOrThrow()));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).bounds(4, 4, 40, 14).build());
	}

	@Override
	protected void init() {
		for (int i = 0; i < configWidgets.size(); i++) {
			configWidgets.get(i).addWidget(this, width / 2 + 4, 6 + i * 13, width * 3 / 8 - 10, 12);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		for (int i = 0; i < configWidgets.size(); i++) {
			var n = configWidgets.get(i).config.name;
			graphics.fill(width / 8, 6 + i * 13, width / 2, 18 + i * 13, 0x80000000);
			graphics.drawString(font, n, width / 2 - font.width(n) - 4, 8 + i * 13, 0xFFFFFFFF);
		}
	}

	@Override
	public void removed() {
		super.removed();
		update.accept(instance);
	}
}
