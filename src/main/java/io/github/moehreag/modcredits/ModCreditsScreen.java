package io.github.moehreag.modcredits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.moehreag.modcredits.entries.Entry;
import io.github.moehreag.modcredits.entries.ModEntry;
import io.github.moehreag.modcredits.entries.TextEntry;
import io.github.moehreag.modcredits.entries.TitleEntry;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class ModCreditsScreen extends Screen {

	private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/credits_vignette.png");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
	private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
	private static final float SPEEDUP_FACTOR = 5.0F;
	private static final float SPEEDUP_FACTOR_FAST = 15.0F;
	private static final ResourceLocation POEM_LOCATION = ModCreditsMod.id("texts/poem.txt");
	private static final ResourceLocation POSTCREDITS_LOCATION = ModCreditsMod.id("texts/post-credits.txt");
	public static final MutableComponent MODS_CONTRIBUTORS_HEADER = Component.translatable("mods_contributors_header");
	public static final MutableComponent MODS_CONTRIBUTOR_HEADER = Component.translatable("mods_contributor_header");
	public static final MutableComponent MODS_AUTHOR_HEADER = Component.translatable("mods_author_header");
	public static final MutableComponent MODS_AUTHORS_HEADER = Component.translatable("mods_authors_header");

	private final Screen parent;
	private final boolean poem;
	private double scroll;
	private List<Entry> entries;
	private int totalScrollLength;
	private boolean speedupActive;
	private final IntSet speedupModifiers = new IntOpenHashSet();
	private float scrollSpeed;
	private final float unmodifiedScrollSpeed;
	private int direction;
	private int previousDir;

	public ModCreditsScreen(Screen parent, boolean poem) {
		super(GameNarrator.NO_TITLE);
		this.parent = parent;
		this.poem = poem;
		if (!poem) {
			this.unmodifiedScrollSpeed = 0.75F;
		} else {
			this.unmodifiedScrollSpeed = 0.5F;
		}

		this.direction = 1;
		this.scrollSpeed = this.unmodifiedScrollSpeed;
	}

	private float calculateScrollSpeed() {
		return this.speedupActive
				? this.unmodifiedScrollSpeed * (SPEEDUP_FACTOR + (float) this.speedupModifiers.size() * SPEEDUP_FACTOR_FAST) * (float) this.direction
				: this.unmodifiedScrollSpeed * (float) this.direction;
	}

	@Override
	public void tick() {
		this.minecraft.getMusicManager().tick();
		this.minecraft.getSoundManager().tick(false);
		float f = (float) (this.totalScrollLength + this.height);
		if (this.scroll > f) {
			this.onClose();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_UP) {
			if (direction > -1) {
				setDirection(-1);
			} else {
				speedupActive = true;
			}
		} else if (keyCode == InputConstants.KEY_LCONTROL || keyCode == InputConstants.KEY_RCONTROL) {
			this.speedupModifiers.add(keyCode);
		} else if (keyCode == InputConstants.KEY_SPACE) {
			this.speedupActive = true;
		} else if (keyCode == InputConstants.KEY_PAGEDOWN) {
			scroll += this.height;
		} else if (keyCode == InputConstants.KEY_PAGEUP) {
			scroll -= this.height;
		} else if (keyCode == InputConstants.KEY_END) {
			scroll = totalScrollLength;
		} else if (keyCode == InputConstants.KEY_HOME) {
			scroll = 0;
		} else if (keyCode == InputConstants.KEY_DOWN) {
			if (direction < 1) {
				setDirection(1);
			} else {
				speedupActive = true;
			}
		} else if (keyCode == InputConstants.KEY_PAUSE || keyCode == InputConstants.KEY_P) {
			if (direction == 0) {
				setDirection(previousDir);
			} else {
				setDirection(0);
			}
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void setDirection(int newDir) {
		previousDir = direction;
		direction = newDir;
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_UP) {
			setDirection(1);
		} else if (keyCode == InputConstants.KEY_SPACE) {
			this.speedupActive = false;
		} else if (keyCode == InputConstants.KEY_LCONTROL || keyCode == InputConstants.KEY_RCONTROL) {
			this.speedupModifiers.remove(keyCode);
		} else if (keyCode == InputConstants.KEY_DOWN) {
			speedupActive = false;
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		entries.forEach(Entry::close);
		minecraft.setScreen(parent);
	}

	@Override
	protected void init() {
		if (this.entries == null) {
			this.entries = new ArrayList<>();

			if (poem) {
				loadPoemFile(POEM_LOCATION);
			}
			addCredits();
			if (poem) {
				loadPoemFile(POSTCREDITS_LOCATION);
			}

			this.totalScrollLength = this.entries.stream().mapToInt(Entry::getHeight).sum();
		}
	}

	private void loadPoemFile(ResourceLocation rl) {
		RandomSource random = RandomSource.create(81345471L);
		try (var reader = minecraft.getResourceManager().openAsReader(rl)) {
			List<FormattedCharSequence> entryLines = new ArrayList<>();
			reader.lines().map(s -> s.replace("PLAYERNAME", minecraft.getUser().getName()))
					.map(string -> {
						int i;
						while ((i = string.indexOf(OBFUSCATE_TOKEN)) != -1) {
							String head = string.substring(0, i);
							String tail = string.substring(i + OBFUSCATE_TOKEN.length());
							string = head + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + tail;
						}
						return string;
					})
					.forEach(text -> {
						entryLines.addAll(font.split(Component.literal(text), 256));
						entryLines.add(FormattedCharSequence.EMPTY);
					});
			addEntry(new TextEntry(entryLines));
		} catch (IOException e) {
			LOGGER.error("Failed to load poem from file!", e);
		}
	}

	private void addCredits() {
		addEntry(new TitleEntry(List.of(SECTION_HEADING.getVisualOrderText(),
				Component.translatable("mods_section_header").withStyle(ChatFormatting.YELLOW).getVisualOrderText(),
				SECTION_HEADING.getVisualOrderText(),
				FormattedCharSequence.EMPTY,
				FormattedCharSequence.EMPTY
		)));

		getModEntries();
	}

	private void addEntry(Entry e) {
		entries.add(e);
	}

	static ResourceLocation getModIcon(ModContainer container) {
		var opt = container.getMetadata().getIconPath(16);
		if (opt.isPresent()) {
			String icon = opt.get();
			try (var in = ModCreditsScreen.class.getResourceAsStream("/" + icon)) {
				if (in != null) {
					var rl = ModCreditsMod.id("mod_icon_" + container.getMetadata().getId());
					Minecraft.getInstance().getTextureManager().register(rl, new DynamicTexture(NativeImage.read(in)));
					return rl;
				}
			} catch (IOException e) {
				LOGGER.warn("Failed to read mod icon of {}!", container.getMetadata().getName(), e);
			}
		}
		return null;
	}

	private static void addPeople(Collection<Person> people, List<ModEntry.Line> lines) {
		people.forEach(p ->
				lines.add(new ModEntry.Line(Component.literal(p.getName()).withStyle(ChatFormatting.WHITE), 44)));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (direction == 0 && scroll < totalScrollLength) { // paused
			scroll -= Math.signum(scrollY)*12;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.blit(RenderType::vignette, VIGNETTE_LOCATION, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
		this.scroll = Math.max(0.0F, this.scroll + partialTick * this.scrollSpeed);
		double shift = -this.scroll;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, shift, 0.0F);
		int currentY = this.height*4/5;

		for (int l = 0; l < this.entries.size(); l++) {
			var entry = entries.get(l);

			int entryHeight = entry.getHeight();
			if (l == this.entries.size() - 1) {
				double g = currentY + shift - (this.height / 2f - entryHeight / 2f);
				if (g < 0.0F) {
					guiGraphics.pose().translate(0.0F, -g, 0.0F);
				}
			}

			if (currentY + shift + entryHeight + 8.0F > 0.0 && currentY + shift < this.height) {
				currentY = entry.render(this, guiGraphics, currentY);
			} else {
				currentY += entryHeight;
			}

		}

		guiGraphics.pose().popPose();
	}

	private void getModEntries() {
		List<String> idBlackList = List.of("minecraft", "java", "fabric-loader");
		final boolean[] rightText = {false};
		var entrypoints = FabricLoader.getInstance().getEntrypointContainers("moehreag-modcredits", ModCreditsApi.class)
				.stream().collect(Collectors.toMap(c -> c.getProvider().getMetadata().getId(), EntrypointContainer::getEntrypoint));
		FabricLoader.getInstance().getAllMods().stream()
				.filter(m -> m.getContainingMod().isEmpty()).filter(m -> {
					String id = m.getMetadata().getId();
					return !idBlackList.contains(id);
				}).sorted(Comparator.comparing(s -> s.getMetadata().getName())).forEach(mod -> {
					if (entrypoints.containsKey(mod.getMetadata().getId())) {
						addEntry(entrypoints.get(mod.getMetadata().getId()).createEntry(mod, rightText[0]));
					} else {
						addEntry(createModEntry(mod, rightText[0]));
					}
					rightText[0] = !rightText[0];
				});
	}

	private static void readCustomFMJProperty(List<ModEntry.Line> lines, ModContainer mod, String property, Function<String, Component> lineFunction) {
		if (mod.getMetadata().containsCustomValue(property)) {
			var custom = mod.getMetadata().getCustomValue(property);
			boolean[] modified = {false};
			if (custom.getType() == CustomValue.CvType.OBJECT) {
				var customObj = custom.getAsObject();
				customObj.forEach(e -> {
					modified[0] = true;
					addLine(lines, lineFunction.apply(e.getKey()));
					var val = e.getValue();
					if (val.getType() == CustomValue.CvType.ARRAY) {
						val.getAsArray().forEach(v -> addLine(lines, lineFunction.apply(v.getAsString()), 44));
					} else if (val.getType() == CustomValue.CvType.STRING) {
						addLine(lines, lineFunction.apply(val.getAsString()), 44);
					}
				});
			} else if (custom.getType() == CustomValue.CvType.STRING) {
				addLine(lines, lineFunction.apply(custom.getAsString()));
			} else if (custom.getType() == CustomValue.CvType.ARRAY) {
				custom.getAsArray().forEach(v -> addLine(lines, lineFunction.apply(v.getAsString())));
			}
			if (modified[0]) {
				lines.add(ModEntry.Line.EMPTY_LINE);
				lines.add(ModEntry.Line.EMPTY_LINE);
			}
		}
	}

	private static void addLine(List<ModEntry.Line> lines, Component component) {
		addLine(lines, component, 0);
	}

	private static void addLine(List<ModEntry.Line> lines, Component c, int offset) {
		Minecraft.getInstance().font.split(c, 256).stream()
				.map(ch -> new ModEntry.Line(ch, offset))
				.forEach(lines::add);
	}

	static Entry createModEntry(ModContainer mod, boolean rightText) {
		List<ModEntry.Line> entryLines = new ArrayList<>();
		entryLines.add(ModEntry.Line.EMPTY_LINE);
		entryLines.add(ModEntry.Line.EMPTY_LINE);

		readCustomFMJProperty(entryLines, mod, "moehreag-modcredits:description", Component::literal);
		readCustomFMJProperty(entryLines, mod, "moehreag-modcredits:description-keys", Component::translatable);

		if (!mod.getMetadata().getAuthors().isEmpty()) {
			if (mod.getMetadata().getAuthors().size() > 1) {
				addLine(entryLines, MODS_AUTHORS_HEADER.withStyle(ChatFormatting.GRAY));
			} else {
				addLine(entryLines, MODS_AUTHOR_HEADER.withStyle(ChatFormatting.GRAY));
			}
			addPeople(mod.getMetadata().getAuthors(), entryLines);
			entryLines.add(ModEntry.Line.EMPTY_LINE);
		}
		entryLines.add(ModEntry.Line.EMPTY_LINE);

		if (!mod.getMetadata().getContributors().isEmpty()) {
			if (mod.getMetadata().getContributors().size() > 1) {
				addLine(entryLines, MODS_CONTRIBUTORS_HEADER.withStyle(ChatFormatting.GRAY));
			} else {
				addLine(entryLines, MODS_CONTRIBUTOR_HEADER.withStyle(ChatFormatting.GRAY));
			}
			addPeople(mod.getMetadata().getContributors(), entryLines);
			entryLines.add(ModEntry.Line.EMPTY_LINE);
		}
		entryLines.add(ModEntry.Line.EMPTY_LINE);

		return new ModEntry(Component.literal(mod.getMetadata().getName()).withStyle(ChatFormatting.YELLOW),
				entryLines, getModIcon(mod), rightText);
	}
}
