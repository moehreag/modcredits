package io.github.moehreag.modcredits;

import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.JsonConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

@Slf4j
public class ModCreditsMod implements ClientModInitializer {
	public static final ModCreditsMod INSTANCE = new ModCreditsMod();
	public static final String MOD_ID = "moehreag-modcredits";
	private final OptionCategory category = OptionCategory.create(MOD_ID);
	public final BooleanOption enablePoemInCreditsButton = new BooleanOption("enable_poem_in_credits_button", false);
	public final BooleanOption enableModLinks = new BooleanOption("enable_mod_links", true);
	public final BooleanOption showModIcons = new BooleanOption("show_mod_icons", true);
	public final BooleanOption compactMode = new BooleanOption("compact_mode", false);

	@Override
	public void onInitializeClient() {
		category.add(enablePoemInCreditsButton, enableModLinks, showModIcons, compactMode);

		var configManger = new JsonConfigManager(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID.replace("-", "_") + ".json"), category);
		AxolotlClientConfig.getInstance().register(configManger);

		configManger.load();
		configManger.save();
	}

	public static Identifier id(String location) {
		return Identifier.fromNamespaceAndPath(MOD_ID, location);
	}
}
