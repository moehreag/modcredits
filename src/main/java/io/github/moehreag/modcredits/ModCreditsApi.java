package io.github.moehreag.modcredits;

import io.github.moehreag.modcredits.entries.Entry;
import net.fabricmc.loader.api.ModContainer;

public interface ModCreditsApi {

	/**
	 * The default implementation to create an entry for a mod.
	 * @param self The ModContainer for this mod
	 * @param rightText whether the text should be displayed on the right
	 * @return an Entry for this mod
	 */
	static Entry createModEntry(ModContainer self, boolean rightText) {
		return ModCreditsScreen.createModEntry(self, rightText);
	}

	/**
	 * Create the entry on the credits screen for this mod.
	 * @param self The ModContainer for this mod
	 * @param rightText whether the text should be displayed on the right
	 * @return an Entry for this mod
	 *
	 * @see Entry
	 * @see io.github.moehreag.modcredits.entries.ModEntry
	 * @see io.github.moehreag.modcredits.entries.TextEntry
	 * @see io.github.moehreag.modcredits.entries.TitleEntry
	 */
	default Entry createEntry(ModContainer self, boolean rightText) {
		return createModEntry(self, rightText);
	}
}
