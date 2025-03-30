package dev.beast.mods.shimmer.feature.vote;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class NumberVotingScreen extends BaseVotingScreen {
	public class SelectNumberButton extends Button {
		public SelectNumberButton(int x, int y, int number) {
			super(x, y, 40, 40, Component.literal(String.valueOf(number + 1)), button -> pressed(number), DEFAULT_NARRATION);
		}

		@Override
		public void renderString(GuiGraphics graphics, Font font, int color) {
			graphics.drawString(font, getMessage(), getX() + (width - font.width(getMessage())) / 2, getY() + (height - 9) / 2, color, true);
		}
	}

	public final int max;
	public final IntSet unavailable;

	public NumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) {
		super(extraData, title, subtitle);
		this.max = max;
		this.unavailable = new IntOpenHashSet(unavailable);
	}

	@Override
	protected void init() {
		super.init();

		var lists = new ArrayList<ArrayList<Button>>();
		var list = new ArrayList<Button>();

		for (int i = 0; i < max; i++) {
			var button = new SelectNumberButton(0, 0, i);

			if (unavailable.contains(i)) {
				button.active = false;
			}

			list.add(button);

			if (list.size() >= 5) {
				lists.add(list);
				list = new ArrayList<>();
			}
		}

		if (!list.isEmpty()) {
			lists.add(list);
		}

		int gap = 10;
		int cy = (height - 40 * lists.size() - gap * (lists.size() - 1)) / 2;

		for (int y = 0; y < lists.size(); y++) {
			var l = lists.get(y);
			int cx = (width - 40 * l.size() - gap * (l.size() - 1)) / 2;

			for (int x = 0; x < l.size(); x++) {
				var button = l.get(x);
				button.setX(cx + x * (40 + gap));
				button.setY(cy + y * (40 + gap));
				addRenderableWidget(button);
			}
		}
	}
}
