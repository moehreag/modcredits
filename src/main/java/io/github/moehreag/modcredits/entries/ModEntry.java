package io.github.moehreag.modcredits.entries;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public class ModEntry implements Entry {
	private static final Minecraft minecraft = Minecraft.getInstance();
	private final Component title;
	private final List<Line> lines;
	private final Identifier icon;
	private final boolean rightText;
	private final int maxLineWidth;
	private final int height;

	public ModEntry(Component title, List<Line> lines, Identifier icon, boolean rightText) {
		this.title = title;
		this.lines = lines;
		this.icon = icon;
		this.rightText = rightText;
		this.maxLineWidth = lines.stream().map(Line::text).mapToInt(minecraft.font::width).max().orElse(0);
		this.height = lines.size() * 12 + 12;
	}

	@Override
	public int render(Screen screen, GuiGraphics guiGraphics, int y) {
		int width = screen.width;
		int iconSize = minecraft.font.lineHeight * 5;
		guiGraphics.drawCenteredString(minecraft.font, title, width / 2, y, -1);
		y += 12;
		if (icon != null) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, rightText ? width / 2 - 128 : width / 2 + 128 - iconSize, y + 12, 0, 0, iconSize, iconSize, iconSize, iconSize);
		}
		for (Line line : lines) {
			guiGraphics.drawString(minecraft.font, line.text(), rightText ? width / 2 + 128 - maxLineWidth - iconSize + line.offset() : width / 2 - 128 + line.offset(), y, -1);
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
		if (icon != null) {
			minecraft.getTextureManager().release(icon);
		}
	}

	public record Line(FormattedCharSequence text, int offset) {

		public Line(Component component, int offset) {
			this(component.getVisualOrderText(), offset);
		}

		public static final Line EMPTY_LINE = new Line(FormattedCharSequence.EMPTY, 0);
	}
}
