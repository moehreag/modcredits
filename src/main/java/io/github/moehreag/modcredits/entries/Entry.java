package io.github.moehreag.modcredits.entries;

import java.util.function.Consumer;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public interface Entry extends AutoCloseable {
	int render(Screen screen, GuiGraphics guiGraphics, int y, TextCollector collector);

	int getHeight();

	@Override
	default void close() {
	}

	interface TextCollector extends Consumer<Consumer<ActiveTextCollector>> {
		default void accept(int x, int y, Component text) {
			accept(t -> t.accept(x, y, text));
		}

		default void accept(int x, int y, FormattedCharSequence text) {
			accept(t -> t.accept(x, y, text));
		}
	}
}
