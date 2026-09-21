package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.feature.client.URITextures;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldsData;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
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
	public static Set<String> downloadedWorlds = Set.of();
	public static final ImString SEARCH = ImGuiUtils.resizableString();

	private boolean update = true;

	public HubWorldsPanel() {
		super("hub-worlds", "Worlds");
		menuBar = MenuItem.root((graphics, items) -> menuBar(items));
	}

	private void menuBar(List<MenuItem> items) {
		items.add(MenuItem.item(ImIcons.RELOAD, "Update", g -> update = true));

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(200F);
			ImGui.inputTextWithHint("###search", ImIcons.SEARCH + " Search...", SEARCH);
		}));
	}

	@Override
	public int setup(ImGraphics graphics) {
		var viewport = ImGui.getMainViewport();
		float yOff = viewport.getWorkPosY() - viewport.getPosY();
		float popupWidth = viewport.getWorkSizeX();
		float popupHeight = viewport.getWorkSizeY() - yOff;

		if (popupWidth <= 0F || popupHeight <= 0F) {
			return -1;
		}

		ImGui.setNextWindowPos(viewport.getWorkPos().x, viewport.getWorkPosY());
		ImGui.setNextWindowSize(popupWidth, popupHeight);
		return super.setup(graphics) | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDocking;
	}

	@Override
	public void content(ImGraphics graphics) {
		if (update) {
			HubWorldsData.update();
			downloadedWorlds = Set.of();
			update = false;

			Util.ioPool().execute(() -> {
				try (var stream = Files.list(PlatformHelper.CURRENT.getGameDirectory().resolve("saves"))) {
					downloadedWorlds = Set.copyOf(stream.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).toList());
				} catch (Exception ignored) {
				}
			});
		}

		var data = HubWorldsData.CURRENT;

		if (data.fetching() == HubWorldsData.TYPE_FETCHING) {
			ImGui.text("Fetching...");
		} else if (data.fetching() == HubWorldsData.TYPE_ERROR) {
			ImGui.text("Error!");
		} else if (data.fetching() == HubWorldsData.TYPE_DONE) {
			ImGui.text(data.worlds().size() + " Worlds:");
		}

		float textSize = ImGui.getFontSize();
		float scale = ImGuiUtils.getDpiScale();
		var search = SEARCH.get().toLowerCase(Locale.ROOT);

		for (var world : data.worlds()) {
			var project = data.relevantProjects().get(world.project().raw());
			var user = data.relevantUsers().get(world.user().raw());

			if (!world.search(search, project, user)) {
				continue;
			}

			ImGui.separator();
			ImGui.pushID(world.uniqueId());

			ImGui.text(world.world().name());
			graphics.pushStack();
			graphics.setStyleCol(ImGuiCol.Text, 0x66FFFFFF);
			ImGui.text(world.uniqueId());
			graphics.popStack();

			ImGui.image(ImGui.isRectVisible(80F, 80F) ? URITextures.gl(graphics.mc, world.iconUrl().orElse(project != null ? project.smallIconUrl() : null), VidLibTextures.PACK.texturePath()) : 0, 80F, 80F);

			ImGui.sameLine();
			ImGui.dummy(4F, 0F);
			ImGui.sameLine();
			ImGui.beginGroup();

			boolean isDownloaded = downloadedWorlds.contains(world.uniqueId());

			var progressUuid = PROGRESS.get(world.uniqueId());
			var progressItem = progressUuid == null ? null : HubClientGateway.PROGRESS_BARS.get(progressUuid);

			if (progressItem != null) {
				ImGui.beginDisabled();
			}

			if (ImGui.button(ImIcons.DOWNLOAD + " Download###download")) {
				if (progressItem == null) {
					requestDownload(graphics.mc, world, false);
				}
			}

			if (progressItem != null) {
				ImGui.endDisabled();
			}

			if (!isDownloaded) {
				ImGui.beginDisabled();
			}

			if (ImGui.button(ImIcons.RELOAD + " Update Existing###update-existing")) {
				if (isDownloaded) {
					requestDownload(graphics.mc, world, true);
				}
			}

			if (!isDownloaded) {
				ImGui.endDisabled();
			}

			ImGui.spacing();

			var defaultBar = true;

			if (progressItem != null) {
				var p = progressItem.progress.get();
				var s = progressItem.size.get();

				if (s > 0L) {
					defaultBar = false;
					ImGui.progressBar(Math.clamp((float) ((double) p / (double) s), 0F, 1F), 300F, 24F * scale, progressItem.infoText.getName(p, s));
				}
			}

			if (defaultBar) {
				ImGui.progressBar(downloadedWorlds.contains(world.uniqueId()) ? 1F : 0F, 300F, 24F * scale, "");
			}

			ImGui.endGroup();

			ImGui.sameLine();
			ImGui.dummy(4F, 0F);
			ImGui.sameLine();

			ImGui.beginGroup();
			ImGui.text(StringUtils.binaryByteSize(world.world().size()));

			if (project != null) {
				ImGui.image(ImGui.isRectVisible(textSize, textSize) ? URITextures.gl(graphics.mc, project.smallIconUrl(), VidLibTextures.PACK.texturePath()) : 0, textSize, textSize);
				ImGui.sameLine();
				ImGui.text(project.name());
			}

			if (user != null) {
				ImGui.image(ImGui.isRectVisible(textSize, textSize) ? URITextures.gl(graphics.mc, user.avatarUrl().orElse(null), VidLibTextures.ID_CARD.texturePath()) : 0, textSize, textSize);
				ImGui.sameLine();
				ImGui.text(user.name());
			}

			ImGui.endGroup();
			ImGui.spacing();

			ImGui.popID();
		}
	}

	private void requestDownload(Minecraft mc, HubWorldsData.AvailableWorld world, boolean updateExisting) {
		Util.nonCriticalIoPool().execute(() -> {
			try {
				var requestId = UUID.randomUUID();

				if (!HubAPI.MinecraftAPI.postWorldRequest(requestId, world.uniqueId(), HubClientSessionData.ID, updateExisting)) {
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
