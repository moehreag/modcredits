package io.github.moehreag.modcredits.entries;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public interface Entry extends AutoCloseable {
	int render(Screen screen, GuiGraphics guiGraphics, int y);

	int getHeight();

	@Override
	default void close() {
	}
}
