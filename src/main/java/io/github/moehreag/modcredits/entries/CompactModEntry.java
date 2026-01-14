package io.github.moehreag.modcredits.entries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public class CompactModEntry implements Entry {
	private static final Minecraft minecraft = Minecraft.getInstance();
	private final Component title;
	private final List<List<FormattedCharSequence>> lines;
	private final int height;
	private final Identifier icon;
	private static final FormattedCharSequence COLON = FormattedCharSequence.forward(": ", Style.EMPTY),
			JOINER = FormattedCharSequence.forward(", ", Style.EMPTY);

	public CompactModEntry(Component title, List<ModEntry.Line> lines, Identifier icon) {
		this.title = title;
		this.lines = new ArrayList<>();
		this.icon = icon;
		{
			List<FormattedCharSequence> line = new ArrayList<>();
			for (var l : lines) {
				if (l == ModEntry.Line.EMPTY_LINE) {
					continue;
				}
				if (l.offset() == 0 && !line.isEmpty()) {
					this.lines.add(line);
					line = new ArrayList<>();
				}
				line.add(l.text());
			}
			if (!line.isEmpty()) {
				this.lines.add(line);
			}
		}
		var font = minecraft.font;
		var width = minecraft.getWindow().getGuiScaledWidth();
		int y = 0;
		for (var line : this.lines) {
			int w = 0;
			for (int i = 0; i < line.size(); i++) {
				if (i > 1) {
					w += font.width(JOINER);
				} else if (i > 0) {
					w += font.width(COLON);
				}
				var part = line.get(i);
				if (w > width / 2 - 50) {
					y += 10;
					w = 0;
				}
				w += font.width(part);
			}
			y += 12;
		}
		this.height = y + 12;
	}

	@Override
	public int render(Screen screen, GuiGraphics guiGraphics, int y, TextCollector collector) {
		int width = screen.width;
		int iconSize = 20;
		collector.accept(width / 2 - 150, y, title);
		y += 12;
		if (icon != null) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, width / 2 + 128 - iconSize, y - 10, 0, 0, iconSize, iconSize, iconSize, iconSize);
		}
		var font = screen.getFont();
		for (var line : lines) {
			int w = 0;
			for (int i = 0; i < line.size(); i++) {
				if (i > 1) {
					collector.accept(width / 2 - 125 + w, y, JOINER);
					w += font.width(JOINER);
				} else if (i > 0) {
					collector.accept(width / 2 - 125 + w, y, COLON);
					w += font.width(COLON);
				}
				var part = line.get(i);
				if (w > width / 2 - 50) {
					y += 10;
					w = 0;
				}
				collector.accept(width / 2 - 125 + w, y, part);
				w += font.width(part);
			}
			y += 12;
		}
		return y;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void close() {
		if (icon != null) Minecraft.getInstance().getTextureManager().release(icon);
	}
}
