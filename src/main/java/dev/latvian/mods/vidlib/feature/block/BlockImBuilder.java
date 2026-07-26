package dev.latvian.mods.vidlib.feature.block;

import com.mojang.blaze3d.textures.GpuTexture;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.gallery.ItemIcons;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.item.CachedItemData;
import dev.latvian.mods.vidlib.feature.item.ItemKey;
import dev.latvian.mods.vidlib.feature.item.VisualItemKey;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class BlockImBuilder implements ImBuilder<Block> {
	public static final Lazy<List<Block>> BLOCKS = Lazy.of(() -> BuiltInRegistries.BLOCK.stream().filter(b -> b.asItem() != Items.AIR).toList());
	public static final ImString SEARCH = ImGuiUtils.resizableString();
	public static final ImBuilderType<Block> TYPE = BlockImBuilder::new;

	public final Block[] block = new Block[1];
	public SelectedPosition selectedPosition;
	private List<CachedItemData> cachedBlocks;

	public BlockImBuilder() {
		this.block[0] = Blocks.AIR;
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

		var currentStack = new ItemStack(block[0].asItem());
		var currentTex = ItemIcons.getTexture(graphics.mc, VisualItemKey.of(currentStack));

		if (graphics.imageButton(currentTex.getTexture(), 16F, 16F, UV.FULL, 3, null)) {
			ImGui.openPopup("###select-block");
			cachedBlocks = null;
		}

		if (ImGui.isItemHovered() && graphics.beginTooltip()) {
			ImGui.text(block[0].getName().getString());
			graphics.endTooltip();
		}

		if (ImGui.beginPopup("###select-block", ImGuiWindowFlags.AlwaysAutoResize)) {
			ImGui.setNextItemWidth(-1F);

			if (ImGui.inputTextWithHint("###search", "Search...", SEARCH)) {
				cachedBlocks = null;
			}

			if (ImGui.beginChild("###select-block-child", 16F + 52F * 5F + 4F * 4F, 52F * 4.5F + 4F * 4F, false, ImGuiWindowFlags.NoSavedSettings)) {
				if (cachedBlocks == null) {
					ImGui.setScrollY(0F);
					cachedBlocks = new ArrayList<>();
					var ctx = new CachedItemData.Context(graphics.mc);
					var searchText = SEARCH.get().replace(" ", "").toLowerCase();

					for (var b : BLOCKS.get()) {
						var stack = new ItemStack(b.asItem());
						var key = new ItemKey(stack.getItemHolder(), stack.getComponentsPatch());
						var cached = CachedItemData.create(graphics.mc, stack, key, ctx);

						if (cached.search().contains(searchText)) {
							cachedBlocks.add(cached);
						}
					}
				}

				graphics.pushStack();
				graphics.setItemSpacing(4F, 4F);
				ImGui.pushID("###buttons");

				int count = 0;

				for (var item : cachedBlocks) {
					if (count % 5 != 0) {
						ImGui.sameLine();
					}

					ImGui.pushID(count);

					if (ImGui.isRectVisible(52F, 52F)) {
						var tex = ItemIcons.getTexture(graphics.mc, item.visualKey());
						var itemBlock = Block.byItem(item.stack().getItem());
						boolean isSelected = itemBlock == block[0];

						if (graphics.imageButton(
							tex.getTexture(),
							48F, 48F, UV.FULL, 2, null,
							isSelected ? Color.GREEN.withAlpha(80) : Color.TRANSPARENT,
							Color.WHITE)
						) {
							block[0] = itemBlock;
							ImGui.closeCurrentPopup();
							ImGui.popID();
							update = ImUpdate.FULL;
							SEARCH.set("");
							break;
						}

						if (ImGui.isItemHovered() && graphics.beginTooltip()) {
							for (var line : item.tooltip()) {
								graphics.text(line);
							}

							graphics.endTooltip();
						}
					} else {
						graphics.imageButton((GpuTexture) null, 48F, 48F, UV.FULL, 2, null);
					}

					ImGui.popID();
					count++;
				}

				ImGui.popID();
				graphics.popStack();
			}

			ImGui.endChild();
			ImGui.endPopup();
		}

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
