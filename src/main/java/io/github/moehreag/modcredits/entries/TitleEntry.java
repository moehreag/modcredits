package io.github.moehreag.modcredits.entries;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;

public class TitleEntry implements Entry {
	private final List<FormattedCharSequence> lines;

	public TitleEntry(List<FormattedCharSequence> lines) {
		this.lines = lines;
	}

	@Override
	public int render(Screen screen, GuiGraphics guiGraphics, int y) {
		for (FormattedCharSequence line : lines) {
			guiGraphics.drawCenteredString(screen.getFont(), line, screen.width / 2, y, -1);
			y += 12;
		}
		return y;
	}

	@Override
	public int getHeight() {
		return lines.size() * 12;
	}
}
