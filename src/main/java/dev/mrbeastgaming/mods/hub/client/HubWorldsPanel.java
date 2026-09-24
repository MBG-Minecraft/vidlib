package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.URITextures;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.PanelStyle;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldsResponse;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HubWorldsPanel extends Panel {
	public static final HubWorldsPanel INSTANCE = new HubWorldsPanel();
	public static final Map<String, UUID> PROGRESS = new HashMap<>();
	public static Set<String> templates = Set.of();
	public static Set<String> saves = Set.of();
	public static final ImString SEARCH = ImGuiUtils.resizableString();
	public static final HubProject[] PROJECT_FILTER = new HubProject[1];
	public static final HubUser[] USER_FILTER = new HubUser[1];

	public boolean reload = true;

	public HubWorldsPanel() {
		super("hub-worlds", "Worlds");
		style = PanelStyle.FULLSCREEN;
		menuBar = MenuItem.root((graphics, items) -> menuBar(items));
	}

	private void menuBar(List<MenuItem> items) {
		items.add(MenuItem.item(ImIcons.RELOAD, "Reload", g -> reload = true));
		items.add(MenuItem.SEPARATOR);

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(300F);
			ImGui.inputTextWithHint("###search", ImIcons.SEARCH + " Search...", SEARCH);
		}));

		items.add(MenuItem.SEPARATOR);

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(200F);
			graphics.combo("###project-filter", PROJECT_FILTER, "Any Project", HubWorldsResponse.CURRENT.ctx().relevantProjects().values().toArray(HubProject[]::new), HubProject::name);
		}));

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(200F);
			graphics.combo("###user-filter", USER_FILTER, "Any User", HubWorldsResponse.CURRENT.ctx().relevantUsers().values().toArray(HubUser[]::new), HubUser::name);
		}));
	}

	@Override
	public void content(ImGraphics graphics) {
		if (reload) {
			HubWorldsResponse.update(data -> {
				var savesDir = PlatformHelper.CURRENT.getGameDirectory().resolve("saves");
				var templatesDir = PlatformHelper.CURRENT.getGameDirectory().resolve("saves-templates");

				try {
					if (Files.notExists(savesDir)) {
						Files.createDirectories(savesDir);
					}

					if (Files.notExists(templatesDir)) {
						Files.createDirectories(templatesDir);
					}
				} catch (Exception ex) {
				}

				try (var stream = Files.list(savesDir)) {
					saves = Set.copyOf(stream.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).toList());
				} catch (Exception ignored) {
					saves = Set.of();
				}

				try (var stream = Files.list(templatesDir)) {
					templates = Set.copyOf(stream.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).toList());
				} catch (Exception ignored) {
					templates = Set.of();
				}

				PROJECT_FILTER[0] = PROJECT_FILTER[0] == null ? null : data.ctx().relevantProjects().get(PROJECT_FILTER[0].id().raw());
				USER_FILTER[0] = USER_FILTER[0] == null ? null : data.ctx().relevantUsers().get(USER_FILTER[0].id().raw());
			});

			reload = false;
		}

		var data = HubWorldsResponse.CURRENT;

		if (data.fetching() == HubWorldsResponse.TYPE_FETCHING) {
			ImGui.text("Fetching...");
		} else if (data.fetching() == HubWorldsResponse.TYPE_ERROR) {
			ImGui.text("Error!");
		} else if (data.fetching() == HubWorldsResponse.TYPE_DONE) {
			ImGui.text(data.worlds().size() + " Worlds");
		}

		float textSize = ImGui.getFontSize();
		float scale = ImGuiUtils.getDpiScale();
		var search = SEARCH.get().toLowerCase(Locale.ROOT);

		for (var world : data.worlds()) {
			var project = world.project();
			var user = world.user();

			if (PROJECT_FILTER[0] != null && !PROJECT_FILTER[0].id().equals(project.id())) {
				continue;
			}

			if (USER_FILTER[0] != null && !USER_FILTER[0].id().equals(user.id())) {
				continue;
			}

			if (!world.search(search, project, user)) {
				continue;
			}

			ImGui.separator();
			ImGui.pushID(world.uniqueId());

			ImGui.beginGroup();

			ImGui.text(world.world().name());
			graphics.pushStack();
			graphics.setStyleCol(ImGuiCol.Text, 0x66FFFFFF);
			ImGui.text(world.uniqueId());
			graphics.popStack();

			ImGui.image(ImGui.isRectVisible(80F, 80F) ? URITextures.gl(graphics.mc, world.iconUrl().orElse(project != null ? project.smallIconUrl().orElse(null) : null), VidLibTextures.DEFAULT_PROJECT_ICON.texturePath()) : 0, 80F, 80F);

			ImGui.sameLine();
			ImGui.dummy(4F, 0F);
			ImGui.sameLine();
			ImGui.beginGroup();

			boolean hasTemplate = templates.contains(world.uniqueId());
			boolean hasSave = hasTemplate && saves.contains(world.uniqueId());

			var progressUuid = PROGRESS.get(world.uniqueId());
			var progressItem = progressUuid == null ? null : HubClientGateway.PROGRESS_BARS.get(progressUuid);

			if (progressItem != null) {
				ImGui.beginDisabled();
			}

			if (ImGui.button(ImIcons.DOWNLOAD + (hasTemplate ? " Update Template###update-template" : " Download Template###update-template"))) {
				if (progressItem == null) {
					requestDownload(graphics.mc, world);
				}
			}

			if (progressItem != null) {
				ImGui.endDisabled();
			}

			if (!hasTemplate || progressItem != null) {
				ImGui.beginDisabled();
			}

			if (graphics.button(hasSave ? (ImIcons.RELOAD + " Re-create from Template###create-from-template") : (ImIcons.ADD + " Create from Template###create-from-template"), ImColorVariant.GREEN)) {
				if (hasTemplate && progressItem == null) {
					Util.ioPool().execute(() -> {
						var uuid = UUID.randomUUID();
						var item = ProgressQueue.queueSingleItem("Copying...");
						item.setSize(0L);
						item.setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
						item.setStarted();
						HubClientGateway.PROGRESS_BARS.put(uuid, item);
						PROGRESS.put(world.uniqueId(), uuid);

						var src = PlatformHelper.CURRENT.getGameDirectory().resolve("saves-templates").resolve(world.uniqueId());
						var dst = PlatformHelper.CURRENT.getGameDirectory().resolve("saves").resolve(world.uniqueId());

						try {
							if (Files.exists(dst)) {
								IOUtils.deleteRecursively(dst);
							}

							IOUtils.copyDirectory(src, dst, true, item::setSize, item::addProgress);
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						item.setDone();
						HubClientGateway.PROGRESS_BARS.remove(uuid);
						PROGRESS.remove(world.uniqueId());
						VidLib.LOGGER.info("Created " + dst);
						reload = true;
					});
				}
			}

			if (!hasTemplate || progressItem != null) {
				ImGui.endDisabled();
			}

			ImGui.beginDisabled();

			if (graphics.button(ImIcons.STAR + " Favorite###favorite", ImColorVariant.YELLOW)) {
			}

			ImGui.endDisabled();

			ImGui.endGroup();

			ImGui.sameLine();
			ImGui.dummy(4F, 0F);
			ImGui.sameLine();

			ImGui.beginGroup();
			ImGui.text(StringUtils.binaryByteSize(world.world().size()));

			if (project != null) {
				ImGui.image(ImGui.isRectVisible(textSize, textSize) ? URITextures.gl(graphics.mc, project.smallIconUrl().orElse(null), VidLibTextures.DEFAULT_PROJECT_ICON.texturePath()) : 0, textSize, textSize);
				ImGui.sameLine();
				ImGui.text(project.name());
			}

			if (user != null) {
				ImGui.image(ImGui.isRectVisible(textSize, textSize) ? URITextures.gl(graphics.mc, user.avatarUrl().orElse(null), VidLibTextures.ID_CARD.texturePath()) : 0, textSize, textSize);
				ImGui.sameLine();
				ImGui.text(user.name());
			}

			ImGui.endGroup();
			ImGui.endGroup();
			ImGui.sameLine();
			ImGui.dummy(4F, 0F);
			ImGui.sameLine();

			ImGui.beginGroup();
			ImGui.spacing();

			if (progressItem != null) {
				var p = progressItem.progress.get();
				var s = progressItem.size.get();

				if (s > 0L) {
					ImGui.progressBar(Math.clamp((float) ((double) p / (double) s), 0F, 1F), 300F, 24F * scale, progressItem.infoText.getName(p, s));
				}
			}

			ImGui.endGroup();

			ImGui.popID();
		}
	}

	private void requestDownload(Minecraft mc, HubWorldsResponse.AvailableWorld world) {
		Util.nonCriticalIoPool().execute(() -> {
			try {
				var requestId = UUID.randomUUID();

				if (!HubAPI.MinecraftAPI.postWorldRequest(requestId, world.uniqueId(), HubClientSession.CURRENT.id)) {
					return;
				}

				var progressItem = ProgressQueue.queueSingleItem(world.world().name());
				progressItem.setInfoText("Requesting Upload...");
				progressItem.setSize(1L);
				progressItem.setStarted();
				HubClientGateway.PROGRESS_BARS.put(requestId, progressItem);
				mc.execute(() -> PROGRESS.put(world.uniqueId(), requestId));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}
}
