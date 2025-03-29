package dev.beast.mods.shimmer.feature.vote;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;

public class VoteScreen extends Screen {
	public static final ResourceLocation TEXTURE_NO = Shimmer.id("textures/misc/no.png");
	public static final ResourceLocation TEXTURE_NO_OFF = Shimmer.id("textures/misc/no_off.png");
	public static final ResourceLocation TEXTURE_YES = Shimmer.id("textures/misc/yes.png");
	public static final ResourceLocation TEXTURE_YES_OFF = Shimmer.id("textures/misc/yes_off.png");
	public static final ResourceLocation TEXTURE_OUTLINE = Shimmer.id("textures/misc/outline.png");

	public class VoteButton extends Button {
		private final boolean isYes;

		public VoteButton(int x, int y, boolean yes) {
			super(x, y, 64, 64, yes ? yesLabel : noLabel, button -> pressed(yes), DEFAULT_NARRATION);
			this.isYes = yes;
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mx, int my, float delta) {
			boolean isSelected = selected == (isYes ? 1 : 0);

			if (isYes) {
				graphics.blit(RenderType::guiTextured, isSelected || isHovered ? TEXTURE_YES : TEXTURE_YES_OFF, getX(), getY(), 0F, 0F, width, height, width, height);
			} else {
				graphics.blit(RenderType::guiTextured, isSelected || isHovered ? TEXTURE_NO : TEXTURE_NO_OFF, getX(), getY(), 0F, 0F, width, height, width, height);
			}

			if (isSelected) {
				graphics.blit(RenderType::guiTextured, TEXTURE_OUTLINE, getX(), getY(), 0F, 0F, width, height, width, height);
			}

			graphics.drawString(font, getMessage(), getX() + (width - font.width(getMessage())) / 2, getY() + 68, 0xFFFFFFFF, true);
		}
	}

	public final CompoundTag voteData;
	public Component subtitle;
	public Component yesLabel;
	public Component noLabel;
	public Button submitButton;
	public int selected = -1;
	public boolean waiting;

	public VoteScreen(CompoundTag voteData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		super(title);
		this.voteData = voteData;
		this.subtitle = subtitle;
		this.yesLabel = yesLabel;
		this.noLabel = noLabel;
	}

	@Override
	protected void init() {
		super.init();

		submitButton = addRenderableWidget(Button.builder(Component.literal("Submit"), button -> {
			button.active = false;
			waiting = true;

			if (NeoForge.EVENT_BUS.post(new VoteEvent(minecraft.player, voteData, selected == 1)).isCanceled()) {
				minecraft.player.endVote();
			} else {
				minecraft.c2s(new VotePayload(voteData, selected == 1));
				button.setMessage(Component.literal("Vote Submitted!"));
			}
		}).bounds((width - 150) / 2, height - 40, 150, 20).build());

		submitButton.active = selected != -1 && !waiting;

		addRenderableWidget(new VoteButton(width / 3 - 32, height / 2 - 38, false));
		addRenderableWidget(new VoteButton(width * 2 / 3 - 32, height / 2 - 38, true));
	}

	private void pressed(boolean yes) {
		if (!waiting) {
			selected = yes ? 1 : 0;
			submitButton.active = true;
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mx, int my, float delta) {
		super.render(graphics, mx, my, delta);

		graphics.pose().pushPose();
		graphics.pose().translate(width / 2F, 30F, 0F);
		graphics.pose().scale(2F, 2F, 1F);
		graphics.drawString(font, title, -font.width(title) / 2, -4, 0xFFFFFFFF, true);
		graphics.pose().popPose();

		var sub = font.split(subtitle, width - 40);

		for (int i = 0; i < sub.size(); i++) {
			graphics.drawString(font, sub.get(i), (width - font.width(sub.get(i))) / 2, 45 + i * 9, 0xFFFFFFFF, true);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return hasShiftDown() && minecraft.isLocalServer();
	}
}
