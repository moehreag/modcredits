package io.github.moehreag.modcredits.entries;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;

public class TitleEntry implements Entry {
	private final List<FormattedCharSequence> lines;
	private final int height;

	public TitleEntry(List<FormattedCharSequence> lines) {
		this.lines = lines;
		this.height = lines.size() * 12;
	}

	@Override
	public int render(Screen screen, GuiGraphics guiGraphics, int y, TextCollector collector) {
		for (FormattedCharSequence line : lines) {
			collector.accept(screen.width / 2 - screen.getFont().width(line)/2, y, line);
			y += 12;
		}
		return y;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
