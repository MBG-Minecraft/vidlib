package dev.latvian.mods.vidlib.feature.vote;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class YesNoVotingScreen extends BaseVotingScreen {
	public static final ResourceLocation TEXTURE_NO = VidLib.id("textures/misc/no.png");
	public static final ResourceLocation TEXTURE_NO_OFF = VidLib.id("textures/misc/no_off.png");
	public static final ResourceLocation TEXTURE_NO_OUTLINE = VidLib.id("textures/misc/no_outline.png");
	public static final ResourceLocation TEXTURE_YES = VidLib.id("textures/misc/yes.png");
	public static final ResourceLocation TEXTURE_YES_OFF = VidLib.id("textures/misc/yes_off.png");
	public static final ResourceLocation TEXTURE_YES_OUTLINE = VidLib.id("textures/misc/yes_outline.png");

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
				graphics.blit(VidLibRenderTypes.GUI, isSelected || isHovered ? TEXTURE_YES : TEXTURE_YES_OFF, getX(), getY(), 0F, 0F, width, height, width, height);
			} else {
				graphics.blit(VidLibRenderTypes.GUI, isSelected || isHovered ? TEXTURE_NO : TEXTURE_NO_OFF, getX(), getY(), 0F, 0F, width, height, width, height);
			}

			if (isSelected) {
				graphics.blit(VidLibRenderTypes.GUI, isYes ? TEXTURE_YES_OUTLINE : TEXTURE_NO_OUTLINE, getX(), getY(), 0F, 0F, width, height, width, height);
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
}
