package io.github.moehreag.modcredits.entries;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;

public class TextEntry implements Entry {
	private final List<FormattedCharSequence> lines;
	private final int height;

	public TextEntry(List<FormattedCharSequence> lines) {
		this.lines = lines;
		this.height = lines.size() * 12;
	}

	@Override
	public int render(Screen screen, GuiGraphics guiGraphics, int y) {
		for (FormattedCharSequence line : lines) {
			guiGraphics.drawString(screen.getFont(), line, screen.width / 2 - 128, y, -1);
			y += 12;
		}
		return y;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
