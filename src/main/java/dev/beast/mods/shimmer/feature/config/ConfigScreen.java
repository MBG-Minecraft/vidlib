package dev.beast.mods.shimmer.feature.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.JsonUtils;
import net.minecraft.ChatFormatting;
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
			return config.encode(instance, ops).toString();
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
				config.decode(instance, ops, tag);
				editBox.setTextColor(0xFFFFFFFF);
			} catch (Exception ex) {
				editBox.setTextColor(0xFFFF0000);
			}
		}
	}

	public static class ConfigToggleButton<C> extends ConfigContainer<C, Boolean> {
		private static final Component TRUE = Component.literal("True").withStyle(ChatFormatting.GREEN);
		private static final Component FALSE = Component.literal("False").withStyle(ChatFormatting.RED);

		public Button button;

		public ConfigToggleButton(DynamicOps<JsonElement> ops, C instance, ConfigValue<C, Boolean> config) {
			super(ops, instance, config);
		}

		@Override
		public void addWidget(ConfigScreen<C> screen, int x, int y, int w, int h) {
			button = new Button.Builder(config.getter.apply(instance) ? TRUE : FALSE, b -> {
				boolean v = !config.getter.apply(instance);
				config.setter.accept(instance, v);
				button.setMessage(v ? TRUE : FALSE);
			}).bounds(x, y, w, h).build();

			screen.addRenderableWidget(button);
		}
	}

	public static class ConfigFloatSlider<C> extends ConfigContainer<C, Float> {
		public AbstractSliderButton slider;

		public ConfigFloatSlider(DynamicOps<JsonElement> ops, C instance, ConfigValue<C, Float> config) {
			super(ops, instance, config);
		}

		@Override
		public void addWidget(ConfigScreen<C> screen, int x, int y, int w, int h) {
			slider = new AbstractSliderButton(x, y, w, h, Component.literal(KMath.format(config.getter.apply(instance))), config.getter.apply(instance)) {
				@Override
				protected void updateMessage() {
					setMessage(Component.literal(KMath.format(config.getter.apply(instance))));
				}

				@Override
				protected void applyValue() {
					config.setter.accept(instance, (float) value);
				}
			};

			screen.addRenderableWidget(slider);
		}
	}

	public final C instance;
	public final List<ConfigContainer<C, ?>> configWidgets;
	public final Consumer<C> update;

	public ConfigScreen(DynamicOps<JsonElement> ops, C instance, List<ConfigValue<C, ?>> config, Consumer<C> update) {
		super(Component.empty());
		this.instance = instance;
		this.configWidgets = new ArrayList<>(config.size());
		this.update = update;

		for (var value : config) {
			if (value instanceof BooleanConfigValue b) {
				configWidgets.add(new ConfigToggleButton<>(ops, instance, b));
			} else if (value instanceof FloatConfigValue b && b.slider) {
				configWidgets.add(new ConfigFloatSlider<>(ops, instance, b));
			} else {
				configWidgets.add(new ConfigEditBox<>(ops, instance, value));
			}
		}
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
