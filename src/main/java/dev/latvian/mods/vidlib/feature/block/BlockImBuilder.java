package dev.latvian.mods.vidlib.feature.block;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockImBuilder implements ImBuilder<Block> {
	public static final Lazy<List<Block>> BLOCKS = Lazy.of(() -> BuiltInRegistries.BLOCK.stream().toList());
	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final Block[] block = new Block[1];
	public SelectedPosition selectedPosition;

	public BlockImBuilder(@Nullable Block defaultType) {
		this.block[0] = defaultType;
	}

	@Override
	public void set(Block value) {
		block[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		selectedPosition = null;
		var update = ImUpdate.NONE;

		if (ImGui.button(ImIcons.TARGET + "###pick-block")) {
			var mc = Minecraft.getInstance();

			if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
				block[0] = mc.level.getBlockState(hit.getBlockPos()).getBlock();
				update = ImUpdate.FULL;
				selectedPosition = SelectedPosition.CURSOR;
			}
		}

		ImGui.sameLine();

		update = update.or(graphics.combo("###block", block, BLOCKS.get(), e -> I18n.get(e.getDescriptionId()), SEARCH));
		return update;
	}

	@Override
	public boolean isValid() {
		return block[0] != null;
	}

	@Override
	public Block build() {
		return block[0];
	}
}
