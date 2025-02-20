package io.github.moehreag.modcredits;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;

@Slf4j
public class ModCreditsMod implements ClientModInitializer {
	public static final String MOD_ID = "moehreag-modcredits";

	@Override
	public void onInitializeClient() {

	}

	public static ResourceLocation id(String location) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, location);
	}
}
