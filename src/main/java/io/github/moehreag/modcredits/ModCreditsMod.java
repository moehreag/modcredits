package io.github.moehreag.modcredits;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;

@Slf4j
public class ModCreditsMod implements ClientModInitializer {
	public static final String MOD_ID = "moehreag-modcredits";

	@Override
	public void onInitializeClient() {

	}

	public static Identifier id(String location) {
		return Identifier.fromNamespaceAndPath(MOD_ID, location);
	}
}
