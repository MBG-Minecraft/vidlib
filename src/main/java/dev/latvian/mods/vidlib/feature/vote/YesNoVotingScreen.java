package dev.latvian.mods.vidlib.feature.vote;

import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class YesNoVotingScreen extends BaseVotingScreen {
	public class VoteButton extends Button {
		private final boolean isYes;

		public VoteButton(int x, int y, boolean yes) {
			super(x, y, 128, 128, yes ? yesLabel : noLabel, button -> pressed(yes ? 1 : 0), DEFAULT_NARRATION);
			this.isYes = yes;
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mx, int my, float delta) {
			boolean isSelected = selected == (isYes ? 1 : 0);

			if (isYes) {
				graphics.blit(VidLibRenderTypes.GUI, isSelected || isHovered ? VidLibTextures.YES : VidLibTextures.YES_OFF, getX(), getY(), 0F, 0F, width, height, width, height);
			} else {
				graphics.blit(VidLibRenderTypes.GUI, isSelected || isHovered ? VidLibTextures.NO : VidLibTextures.NO_OFF, getX(), getY(), 0F, 0F, width, height, width, height);
			}

			if (isSelected) {
				graphics.blit(VidLibRenderTypes.GUI, isYes ? VidLibTextures.YES_OUTLINE : VidLibTextures.NO_OUTLINE, getX(), getY(), 0F, 0F, width, height, width, height);
			}

			graphics.drawString(font, getMessage(), getX() + (width - font.width(getMessage())) / 2, getY() + height + 6, 0xFFFFFFFF, true);
		}
	}

	public Component yesLabel;
	public Component noLabel;

	public YesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		super(extraData, title, subtitle);
		this.yesLabel = yesLabel;
		this.noLabel = noLabel;
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(new VoteButton(width / 3 - 64, height / 2 - 64 - 14, false));
		addRenderableWidget(new VoteButton(width * 2 / 3 - 64, height / 2 - 64 - 14, true));
	}

	@Override
	public void sendPayload() {
		if (extraData.isEmpty()) {
			minecraft.runClientCommand(selected == 0 ? "vote no" : "vote yes");
		} else {
			minecraft.runClientCommand((selected == 0 ? "vote no" : "vote yes") + " " + extraData);
		}
	}
}
