package io.github.moehreag.modcredits.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.moehreag.modcredits.ModCreditsMod;
import io.github.moehreag.modcredits.ModCreditsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreditsAndAttributionScreen.class)
public abstract class CreditsAndAttributionScreenMixin extends Screen {

	private CreditsAndAttributionScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToFooter(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
	private void addModCreditsButton(CallbackInfo ci, @Local(name = "content") LinearLayout layout) {
		layout.addChild(Button.builder(Component.translatable("mod_credits_button"),
				btn -> minecraft.gui.setScreen(new ModCreditsScreen(this, ModCreditsMod.INSTANCE.enablePoemInCreditsButton.get())))
				.width(210).build());
	}
}
