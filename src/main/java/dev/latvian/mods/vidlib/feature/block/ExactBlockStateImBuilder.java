package dev.latvian.mods.vidlib.feature.block;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExactBlockStateImBuilder implements ImBuilder<BlockState> {
	public final BlockImBuilder block;
	public final Map<Property<?>, Object> properties;
	public SelectedPosition selectedPosition;
	private BlockState blockState;

	public ExactBlockStateImBuilder(@Nullable BlockState defaultState) {
		this.block = new BlockImBuilder(null);
		this.properties = new LinkedHashMap<>();
		this.blockState = defaultState;

		if (defaultState != null) {
			set(defaultState);
		}
	}

	@Override
	public void set(@Nullable BlockState state) {
		properties.clear();

		if (state == null) {
			block.set(null);
			return;
		}

		block.set(state.getBlock());

		for (var property : state.getProperties()) {
			var value = state.getValue(property);

			if (property instanceof BooleanProperty) {
				properties.put(property, new ImBoolean((Boolean) value));
			} else if (property instanceof IntegerProperty) {
				properties.put(property, new ImInt((Integer) value));
			} else {
				properties.put(property, new Object[]{value});
			}
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		selectedPosition = null;
		var update = ImUpdate.NONE;

		ImGui.alignTextToFramePadding();
		ImGui.text("Block");
		ImGui.sameLine();
		ImGui.pushID("###block");
		var prevBlock = block.isValid() ? block.build() : null;
		update = update.or(block.imgui(graphics));
		ImGui.popID();

		selectedPosition = block.selectedPosition;

		if (selectedPosition == SelectedPosition.CURSOR) {
			var mc = Minecraft.getInstance();

			if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
				set(mc.level.getBlockState(hit.getBlockPos()));
				update = ImUpdate.FULL;
				prevBlock = block.build();
			}
		}

		if (block.isValid() && prevBlock != block.build()) {
			properties.clear();

			var defState = block.build().defaultBlockState();

			for (var property : defState.getProperties()) {
				var value = defState.getValue(property);

				if (property instanceof BooleanProperty) {
					properties.put(property, new ImBoolean((Boolean) value));
				} else if (property instanceof IntegerProperty) {
					properties.put(property, new ImInt((Integer) value));
				} else {
					properties.put(property, new Object[]{value});
				}
			}
		}

		if (!properties.isEmpty()) {
			ImGui.text("Properties");
			ImGui.pushID("###properties");
			ImGui.indent();

			for (var entry : properties.entrySet()) {
				var property = entry.getKey();
				var value = entry.getValue();

				ImGui.alignTextToFramePadding();
				ImGui.text(property.getName());
				ImGui.sameLine();

				if (property instanceof BooleanProperty) {
					update = update.or(ImGui.checkbox("###" + property.getName(), (ImBoolean) value));
				} else if (property instanceof IntegerProperty prop) {
					int min = prop.getPossibleValues().getFirst();
					int max = prop.getPossibleValues().getLast();

					ImGui.sliderInt("###" + property.getName(), ((ImInt) value).getData(), min, max);
					update = update.orItemEdit();
				} else {
					update = update.or(graphics.combo("###" + property.getName(), "", (Object[]) value, property.getPossibleValues()));
				}
			}

			ImGui.unindent();
			ImGui.popID();
		}

		if (update.isAny()) {
			blockState = null;

			if (block.isValid()) {
				blockState = block.build().defaultBlockState();

				for (var entry : properties.entrySet()) {
					var property = entry.getKey();
					var value = entry.getValue();

					if (property instanceof BooleanProperty) {
						blockState = blockState.setValue((BooleanProperty) property, ((ImBoolean) value).get());
					} else if (property instanceof IntegerProperty) {
						blockState = blockState.setValue((IntegerProperty) property, ((ImInt) value).get());
					} else {
						blockState = blockState.setValue(property, Cast.to(((Object[]) value)[0]));
					}
				}
			}
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return blockState != null;
	}

	@Override
	public BlockState build() {
		return blockState;
	}
}
