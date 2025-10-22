package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.entity.filter.ProfileEntityFilter;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color3ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfField;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldPanel;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.PositionType;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;

public class EntityExplorerPanel extends AdminPanel {
	public static final EntityExplorerPanel INSTANCE = new EntityExplorerPanel();

	public final ImBoolean sortByClosest;
	public final ImBoolean onlyPlayers;
	public final ImString tagInput;

	public EntityExplorerPanel() {
		super("entity-explorer", "Entity Explorer");
		this.sortByClosest = new ImBoolean(false);
		this.onlyPlayers = new ImBoolean(false);
		this.tagInput = ImGuiUtils.resizableString();
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		var allEntities = new ArrayList<Entity>();

		if (onlyPlayers.get()) {
			allEntities.addAll(graphics.mc.level.players());
		} else {
			for (var entity : graphics.mc.level.allEntities()) {
				allEntities.add(entity);
			}
		}

		var delta = graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		ImGui.text("Entities: %,d".formatted(allEntities.size()));
		ImGui.checkbox("Sort by Closest", sortByClosest);
		ImGui.checkbox("Only Players", onlyPlayers);

		if (sortByClosest.get() && allEntities.size() >= 2) {
			var cam = graphics.mc.gameRenderer.getMainCamera().getPosition();
			allEntities.sort(Comparator.comparingDouble(p -> p.getEyePosition(delta).distanceToSqr(cam)));
		}

		ImGui.pushItemWidth(-1F);

		for (var entity : allEntities) {
			ImGui.pushID(entity.getId());

			if (graphics.collapsingHeader(entity.getName().getString() + " #" + entity.getId(), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
				// ClientProps.OPEN_PROPS.add(entity.id);
				imgui(graphics, entity, delta);
			}

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}

	public static void imgui(ImGraphics graphics, Entity entity, float delta) {
		if (entity == null || graphics.mc.player == null) {
			return;
		}

		var session = graphics.mc.player.vl$sessionData();

		if (!graphics.isReplay) {
			if (ImGui.smallButton("Copy UUID")) {
				ImGui.setClipboardText(entity.getUUID().toString());
			}

			ImGui.sameLine();

			if (ImGui.smallButton("Copy Network ID")) {
				ImGui.setClipboardText(Integer.toString(entity.getId()));
			}
		}

		if (!graphics.isReplay) {
			if (graphics.smallButton("Kill", ImColorVariant.RED)) {
				graphics.mc.runClientCommand("kill " + entity.getUUID());
			}

			if (entity instanceof Player) {
				ImGui.beginDisabled();
			}

			ImGui.sameLine();

			if (graphics.smallButton("Discard", ImColorVariant.RED)) {
				graphics.mc.runClientCommand("discard " + entity.getUUID());
			}

			if (entity instanceof Player) {
				ImGui.endDisabled();
			}

			ImGui.sameLine();
		}

		if (!graphics.isReplay) {
			if (entity == graphics.mc.player) {
				ImGui.beginDisabled();
			}

			if (graphics.smallButton("TP To", ImColorVariant.DARK_PURPLE)) {
				graphics.mc.runClientCommand("tp " + entity.getUUID());
			}

			ImGui.sameLine();

			if (graphics.smallButton("TP Here", ImColorVariant.DARK_PURPLE)) {
				graphics.mc.runClientCommand("tp " + entity.getUUID() + " @s");
			}

			if (entity == graphics.mc.player) {
				ImGui.endDisabled();
			}
		}

		if (entity instanceof Player player) {
			if (ImGui.button("Edit Player Data###vidlib-edit-player-data", -1F, 0F)) {
				new PlayerDataConfigPanel(player.getScoreboardName(), player.vl$sessionData().dataMap).open();
			}
		}

		if (DepthOfField.OVERRIDE_ENABLED.get() && ImGui.button(ImIcons.APERTURE + " Focus DoF###focus-dof")) {
			if (entity instanceof Player player) {
				DepthOfField.OVERRIDE = DepthOfField.OVERRIDE.withFocus(KVector.following(new ProfileEntityFilter(player.getGameProfile()), PositionType.EYES));
			} else {
				DepthOfField.OVERRIDE = DepthOfField.OVERRIDE.withFocus(KVector.following(entity, PositionType.EYES));
			}

			DepthOfFieldPanel.INSTANCE.builder.set(DepthOfField.OVERRIDE);
		}

		var team = entity.getTeam();

		if (ImGui.button("Team: " + (team == null ? "None" : team.getName()) + "###vidlib-entity-team")) {
			ImGui.openPopup("###vidlib-edit-team-popup");
		}

		if (ImGui.beginPopup("Edit Team###vidlib-edit-team-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
			if (ImGui.beginListBox("###teams", 200F, 120F)) {
				if (ImGui.selectable(ImIcons.CLOSE + " None", team == null)) {
					graphics.mc.runClientCommand("team leave " + entity.getUUID());
					ImGui.closeCurrentPopup();
				}

				for (var teamName : graphics.mc.level.getScoreboard().getTeamNames()) {
					if (ImGui.selectable(teamName, team != null && team.getName().equals(teamName))) {
						graphics.mc.runClientCommand("team join " + teamName + " " + entity.getUUID());
						ImGui.closeCurrentPopup();
					}
				}

				ImGui.endListBox();
			}

			ImGui.endPopup();
		}

		if (entity instanceof Player) {
			ImGui.sameLine();

			var tags = entity.getTags();

			if (ImGui.button(tags.size() + " Tags###vidlib-entity-tags")) {
				ImGui.openPopup("###vidlib-edit-tags-popup");
			}

			if (!tags.isEmpty()) {
				ImGui.sameLine();
				ImGui.alignTextToFramePadding();
				ImGui.text(String.join(", ", tags));
			}

			if (ImGui.beginPopup("Edit Team###vidlib-edit-tags-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
				if (tags.isEmpty()) {
					ImGui.text("No tags");
				}

				for (var tag : tags) {
					ImGui.text(tag);
					ImGui.sameLine();
					graphics.pushStack();
					graphics.setRedButton();

					if (ImGui.smallButton("-###remove-tag-" + tag)) {
						graphics.mc.runClientCommand("tag " + entity.getUUID() + " remove " + tag);
					}

					graphics.popStack();
				}

				ImGui.inputText("###add-tag-input", EntityExplorerPanel.INSTANCE.tagInput);
				boolean finished = ImGui.isItemDeactivatedAfterEdit();

				ImGui.sameLine();

				if (ImGui.button("+###add-tag") || finished) {
					var tag = EntityExplorerPanel.INSTANCE.tagInput.get();
					EntityExplorerPanel.INSTANCE.tagInput.set("");
					graphics.mc.runClientCommand("tag " + entity.getUUID() + " add " + tag);
				}

				ImGui.endPopup();
			}
		}

		if (graphics.isReplay) {
			Pins.imgui(graphics, entity);
		}

		var glowColor = session.glowColors.get(entity.getUUID());

		if (ImGui.checkbox("Override Glow Color###override-glow-color", glowColor != null)) {
			if (glowColor != null) {
				session.glowColors.remove(entity.getUUID());
			} else {
				glowColor = Color.WHITE;
				session.glowColors.put(entity.getUUID(), glowColor);
			}
		}

		if (glowColor != null) {
			var builder = new Color3ImBuilder();
			builder.set(glowColor);

			if (builder.imguiKey(graphics, "Glow Color", "glow-color").isAny()) {
				glowColor = builder.build();
				session.glowColors.put(entity.getUUID(), glowColor);
			}
		}

		entity.imgui(graphics, delta);
	}
}
