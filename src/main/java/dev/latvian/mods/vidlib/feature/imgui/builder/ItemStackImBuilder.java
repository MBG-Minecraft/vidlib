package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.gallery.ItemIcons;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.item.CachedItemData;
import dev.latvian.mods.vidlib.feature.item.ItemKey;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ItemStackImBuilder implements ImBuilder<ItemStack> {
	public static final ImBuilderType<ItemStack> TYPE = () -> new ItemStackImBuilder(false, stack -> true);
	public static final ImBuilderType<ItemStack> TYPE_WITH_COUNT = () -> new ItemStackImBuilder(true, stack -> true);

	public static boolean isEquipment(ItemStack stack, EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
			var i = stack.getItem();
			return i instanceof ShieldItem || i instanceof BowItem || i instanceof FishingRodItem || stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON);
		}

		var e = stack.get(DataComponents.EQUIPPABLE);
		return e != null && e.slot() == slot;
	}

	public static final ImBuilderType<ItemStack> MAIN_HAND_EQUIPMENT_TYPE = () -> new ItemStackImBuilder(false, stack -> isEquipment(stack, EquipmentSlot.MAINHAND));
	public static final ImBuilderType<ItemStack> OFF_HAND_EQUIPMENT_TYPE = () -> new ItemStackImBuilder(false, stack -> isEquipment(stack, EquipmentSlot.OFFHAND));
	public static final ImBuilderType<ItemStack> HEAD_EQUIPMENT_TYPE = () -> new ItemStackImBuilder(false, stack -> isEquipment(stack, EquipmentSlot.HEAD));
	public static final ImBuilderType<ItemStack> CHEST_EQUIPMENT_TYPE = () -> new ItemStackImBuilder(false, stack -> isEquipment(stack, EquipmentSlot.CHEST));
	public static final ImBuilderType<ItemStack> LEGS_EQUIPMENT_TYPE = () -> new ItemStackImBuilder(false, stack -> isEquipment(stack, EquipmentSlot.LEGS));
	public static final ImBuilderType<ItemStack> FEET_EQUIPMENT_TYPE = () -> new ItemStackImBuilder(false, stack -> isEquipment(stack, EquipmentSlot.FEET));

	public static final Lazy<Set<ItemKey>> FAVORITES = Lazy.of(() -> {
		var set = new LinkedHashSet<ItemKey>();
		var path = VidLibPaths.LOCAL.get().resolve("item-favorites.json");

		if (Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				var mc = Minecraft.getInstance();
				var ops = mc.level == null ? JsonOps.INSTANCE : mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
				var json = JsonUtils.read(reader);

				for (var item : ItemStack.OPTIONAL_CODEC.listOf().parse(ops, json).getOrThrow()) {
					if (!item.isEmpty()) {
						set.add(new ItemKey(item.getItemHolder(), item.getComponentsPatch()));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return set;
	});

	public static void saveFavorites() {
		try (var writer = Files.newBufferedWriter(VidLibPaths.LOCAL.get().resolve("item-favorites.json"))) {
			var mc = Minecraft.getInstance();
			var ops = mc.level == null ? JsonOps.INSTANCE : mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
			var json = ItemStack.OPTIONAL_CODEC.listOf().encodeStart(ops, FAVORITES.get().stream().map(ItemKey::toItemStack).toList());
			JsonUtils.write(writer, json.getOrThrow(), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private record RenderedItem(CachedItemData cachedItem, int order, boolean favorite, boolean filtered, boolean isResult) {
	}

	private static Map<ItemKey, CachedItemData> cachedItems = null;

	public final boolean hasCount;
	public final Predicate<ItemStack> filter;
	public final ImInt count;
	public final ImString search;
	public final ImString input;
	private CachedItemData result;
	private List<RenderedItem> renderedItems;

	public ItemStackImBuilder(boolean hasCount, Predicate<ItemStack> filter) {
		this.hasCount = hasCount;
		this.filter = filter;
		this.count = new ImInt(1);
		this.search = ImGuiUtils.resizableString();
		this.input = ImGuiUtils.resizableString();
		this.result = null;
	}

	@Override
	public void set(ItemStack value) {
		if (value != null) {
			if (value.isEmpty()) {
				count.set(1);
				result = CachedItemData.AIR;
			} else {
				var mc = Minecraft.getInstance();
				var ctx = new CachedItemData.Context(mc);

				count.set(Math.max(1, value.getCount()));

				var key = new ItemKey(value.getItemHolder(), value.getComponentsPatch());
				result = CachedItemData.create(mc, value.getCount() == 1 ? value : value.copyWithCount(1), key, ctx);
			}
		} else {
			count.set(1);
			result = null;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		var currentStackTex = ItemIcons.getTexture(graphics.mc, result.visualKey());

		if (graphics.imageButton(currentStackTex.getTexture(), 16F, 16F, UV.FULL, 3, null)) {
			ImGui.openPopup("###select-item");
			cachedItems = null;
		}

		if (ImGui.isItemHovered()) {
			ImGui.beginTooltip();

			if (result != null) {
				for (var line : result.tooltip()) {
					graphics.text(line);
				}
			} else {
				ImGui.text("Select Item...");
			}

			ImGui.endTooltip();
		}

		if (ImGui.beginPopup("###select-item", ImGuiWindowFlags.AlwaysAutoResize)) {
			ImGui.setNextItemWidth(-1F);

			if (ImGui.inputTextWithHint("###search", "Search...", search)) {
				renderedItems = null;
			}

			if (ImGui.beginChild("###select-item-child", 16F + 52F * 5F + 4F * 4F, 52F * 4.5F + 4F * 4F, false, ImGuiWindowFlags.NoSavedSettings)) {
				if (cachedItems == null) {
					ImGui.setScrollY(0F);

					var map = new LinkedHashMap<ItemKey, CachedItemData>();
					map.put(CachedItemData.AIR.key(), CachedItemData.AIR);

					CreativeModeTabs.tryRebuildTabContents(graphics.mc.player == null ? FeatureFlagSet.of() : graphics.mc.player.connection.enabledFeatures(), true, graphics.mc.level == null ? MiscUtils.STATIC_REGISTRY_ACCESS : graphics.mc.level.registryAccess());

					var stacks = CreativeModeTabs.searchTab().getDisplayItems();
					var ctx = new CachedItemData.Context(graphics.mc);

					for (var stack : stacks) {
						var key = new ItemKey(stack.getItemHolder(), stack.getComponentsPatch());

						if (!map.containsKey(key)) {
							map.put(key, CachedItemData.create(graphics.mc, stack, key, ctx));
						}
					}

					for (var key : FAVORITES.get()) {
						if (!map.containsKey(key)) {
							map.put(key, CachedItemData.create(graphics.mc, key.toItemStack(), key, ctx));
						}
					}

					cachedItems = map;
					renderedItems = null;
				}

				graphics.pushStack();
				graphics.setStyleVar(ImGuiStyleVar.ItemSpacing, 4F, 4F);

				if (renderedItems == null) {
					renderedItems = new ArrayList<>();
					var searchText = search.get().replace(" ", "").toLowerCase(Locale.ROOT);

					renderedItems.add(new RenderedItem(CachedItemData.AIR, -10, false, true, result == null || result.matches(CachedItemData.AIR)));

					if (result != null && !result.matches(CachedItemData.AIR)) {
						renderedItems.add(new RenderedItem(result, -5, FAVORITES.get().contains(result.key()), filter.test(result.stack()), true));
					}

					for (var item : cachedItems.values()) {
						if (item != CachedItemData.AIR && !item.matches(result) && item.search().contains(searchText)) {
							var filtered = filter.test(item.stack());
							var favorite = FAVORITES.get().contains(item.key());
							renderedItems.add(new RenderedItem(item, favorite ? filtered ? 0 : 1 : filtered ? 2 : 3, favorite, filtered, false));
						}
					}

					renderedItems.sort(Comparator.comparingInt(RenderedItem::order));
				}

				ImGui.pushID("###buttons");

				int count = 0;

				for (var item : renderedItems) {
					if (count % 5 != 0) {
						ImGui.sameLine();
					}

					ImGui.pushID(count);

					if (ImGui.isRectVisible(52F, 52F)) {
						var tex = ItemIcons.getTexture(graphics.mc, item.cachedItem.visualKey());

						if (graphics.imageButton(
							tex.getTexture(),
							48F, 48F, UV.FULL, 2,
							item.filtered ? null : ImColorVariant.GRAY,
							item.favorite ? Color.YELLOW.withAlpha(120) : item.isResult ? Color.GREEN.withAlpha(80) : Color.TRANSPARENT,
							Color.WHITE)
						) {
							result = item.cachedItem;
							ImGui.closeCurrentPopup();
							ImGui.popID();
							update = ImUpdate.FULL;
							cachedItems = null;
							break;
						}

						if (ImGui.isItemHovered()) {
							ImGui.beginTooltip();

							for (var line : item.cachedItem.tooltip()) {
								graphics.text(line);
							}

							ImGui.endTooltip();

							if (ImGui.isMouseClicked(1)) {
								ImGui.openPopup("###context-menu");
							}
						}

						if (ImGui.beginPopup("###context-menu", ImGuiWindowFlags.AlwaysAutoResize)) {
							if (ImGui.button(ImIcons.STAR + (item.favorite ? " Unfavorite###favorite" : " Favorite###favorite"))) {
								ImGui.closeCurrentPopup();
								ImGui.endPopup();
								ImGui.popID();

								if (item.favorite) {
									FAVORITES.get().remove(item.cachedItem.key());
								} else {
									FAVORITES.get().add(item.cachedItem.key());
								}

								Util.ioPool().execute(ItemStackImBuilder::saveFavorites);
								cachedItems = null;
								break;
							}

							ImGui.endPopup();
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
			ImGui.setNextItemWidth(-1F);
			ImGui.inputTextWithHint("###input", "Item ID", input);

			if (ImGui.isItemDeactivatedAfterEdit()) {
				try {
					var ctx = new CachedItemData.Context(graphics.mc);
					var parsed = new ItemParser(ctx.registryAccess()).parse(new StringReader(input.get()));
					var value = new ItemStack(parsed.item(), 1, parsed.components());

					if (!value.isEmpty()) {
						var key = new ItemKey(parsed.item(), parsed.components());
						result = CachedItemData.create(graphics.mc, value.getCount() == 1 ? value : value.copyWithCount(1), key, ctx);
						update = ImUpdate.FULL;
						renderedItems = null;
						input.set("");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			ImGui.endPopup();
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return result != null;
	}

	@Override
	public ItemStack build() {
		return result == null || result == CachedItemData.AIR ? ItemStack.EMPTY : result.stack().copyWithCount(count.get());
	}
}
