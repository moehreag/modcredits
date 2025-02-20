package io.github.moehreag.modcredits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
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
	private static final String NAME_PREFIX = "           ";
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

	private FormattedCharSequence emptyLine() {
		return FormattedCharSequence.EMPTY;
	}

	private void addCredits() {
		addEntry(new TitleEntry(List.of(SECTION_HEADING.getVisualOrderText(),
				Component.translatable("mods_section_header").withStyle(ChatFormatting.YELLOW).getVisualOrderText(),
				SECTION_HEADING.getVisualOrderText(),
				emptyLine(),
				emptyLine()
		)));

		List<String> idBlackList = List.of("minecraft", "java", "fabric-loader");
		final boolean[] rightText = {false};
		FabricLoader.getInstance().getAllMods().stream()
				.filter(m -> m.getContainingMod().isEmpty()).filter(m -> {
					String id = m.getMetadata().getId();
					return !idBlackList.contains(id);
				}).sorted(Comparator.comparing(s -> s.getMetadata().getName())).forEach(mod -> {
					addEntry(createModEntry(mod, rightText[0]));
					rightText[0] = !rightText[0];
				});
	}

	private void addEntry(Entry e) {
		entries.add(e);
	}

	private ResourceLocation getModIcon(ModContainer container) {
		var opt = container.getMetadata().getIconPath(16);
		if (opt.isPresent()) {
			String icon = opt.get();
			try (var in = this.getClass().getResourceAsStream("/" + icon)) {
				if (in != null) {
					var rl = ModCreditsMod.id("mod_icon_" + container.getMetadata().getId());
					minecraft.getTextureManager().register(rl, new DynamicTexture(NativeImage.read(in)));
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
				lines.add(new ModEntry.Line(Component.literal(p.getName()).withStyle(ChatFormatting.WHITE).getVisualOrderText(), 44)));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.blit(RenderType::vignette, VIGNETTE_LOCATION, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
		this.scroll = Math.max(0.0F, this.scroll + partialTick * this.scrollSpeed);
		double shift = -this.scroll;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, shift, 0.0F);
		int currentY = this.height;// + 50;

		for (int l = 0; l < this.entries.size(); l++) {
			var entry = entries.get(l);

			int entryHeight = entry.getHeight();
			if (l == this.entries.size() - 1) {
				double g = (float) currentY + shift - (float) (this.height / 2 - entryHeight / 2);
				if (g < 0.0F) {
					guiGraphics.pose().translate(0.0F, -g, 0.0F);
				}
			}

			if ((float) currentY + shift + entryHeight + 8.0F > 0.0F && (float) currentY + shift < (float) this.height) {
				currentY = entry.render(guiGraphics, currentY);
			} else {
				currentY += entryHeight;
			}

		}

		guiGraphics.pose().popPose();
	}

	private interface Entry extends AutoCloseable {
		int render(GuiGraphics guiGraphics, int y);

		int getHeight();

		@Override
		default void close() {
		}
	}

	private class TitleEntry implements Entry {
		private final List<FormattedCharSequence> lines;

		private TitleEntry(List<FormattedCharSequence> lines) {
			this.lines = lines;
		}

		@Override
		public int render(GuiGraphics guiGraphics, int y) {
			for (FormattedCharSequence line : lines) {
				guiGraphics.drawCenteredString(font, line, width / 2, y, -1);
				y += 12;
			}
			return y;
		}

		@Override
		public int getHeight() {
			return lines.size() * 12;
		}
	}

	private class TextEntry implements Entry {
		private final List<FormattedCharSequence> lines;

		private TextEntry(List<FormattedCharSequence> lines) {
			this.lines = lines;
		}

		@Override
		public int render(GuiGraphics guiGraphics, int y) {
			for (FormattedCharSequence line : lines) {
				guiGraphics.drawString(font, line, width / 2 - 128, y, -1);
				y += 12;
			}
			return y;
		}

		@Override
		public int getHeight() {
			return lines.size() * 12;
		}
	}

	private ModEntry createModEntry(ModContainer mod, boolean rightText) {
		List<ModEntry.Line> entryLines = new ArrayList<>();
		entryLines.add(ModEntry.Line.EMPTY_LINE);
		entryLines.add(ModEntry.Line.EMPTY_LINE);

		if (!mod.getMetadata().getAuthors().isEmpty()) {
			if (mod.getMetadata().getAuthors().size() > 1) {
				entryLines.add(new ModEntry.Line(MODS_AUTHORS_HEADER.withStyle(ChatFormatting.GRAY).getVisualOrderText()));
			} else {
				entryLines.add(new ModEntry.Line(MODS_AUTHOR_HEADER.withStyle(ChatFormatting.GRAY).getVisualOrderText()));
			}
			addPeople(mod.getMetadata().getAuthors(), entryLines);
			entryLines.add(ModEntry.Line.EMPTY_LINE);
		}
		entryLines.add(ModEntry.Line.EMPTY_LINE);

		if (!mod.getMetadata().getContributors().isEmpty()) {
			if (mod.getMetadata().getContributors().size() > 1) {
				entryLines.add(new ModEntry.Line(MODS_CONTRIBUTORS_HEADER.withStyle(ChatFormatting.GRAY).getVisualOrderText()));
			} else {
				entryLines.add(new ModEntry.Line(MODS_CONTRIBUTOR_HEADER.withStyle(ChatFormatting.GRAY).getVisualOrderText()));
			}
			addPeople(mod.getMetadata().getContributors(), entryLines);
			entryLines.add(ModEntry.Line.EMPTY_LINE);
		}
		entryLines.add(ModEntry.Line.EMPTY_LINE);

		return new ModEntry(Component.literal(mod.getMetadata().getName()).withStyle(ChatFormatting.YELLOW),
				entryLines, getModIcon(mod), rightText);
	}

	private class ModEntry implements Entry {
		private final Component title;
		private final List<Line> lines;
		private final ResourceLocation icon;
		private final boolean rightText;
		private final int maxLineWidth;

		private ModEntry(Component title, List<Line> lines, ResourceLocation icon, boolean rightText) {
			this.title = title;
			this.lines = lines;
			this.icon = icon;
			this.rightText = rightText;
			this.maxLineWidth = lines.stream().map(Line::text).mapToInt(font::width).max().orElse(0);
		}

		@Override
		public int render(GuiGraphics guiGraphics, int y) {
			int iconSize = font.lineHeight * 5;
			guiGraphics.drawCenteredString(font, title, width / 2, y, -1);
			y += 12;
			if (icon != null) {
				guiGraphics.blit(RenderType::guiTextured, icon, rightText ? width / 2 - 128 : width / 2 + 128 - iconSize, y + 12, 0, 0, iconSize, iconSize, iconSize, iconSize);
			}
			for (Line line : lines) {
				guiGraphics.drawString(font, line.text(), rightText ? width / 2 + 128 - maxLineWidth - iconSize + line.offset() : width / 2 - 128 + line.offset(), y, -1);
				y += 12;
			}
			return y;
		}

		@Override
		public int getHeight() {
			return lines.size() * 12 + 12;
		}

		@Override
		public void close() {
			if (icon != null) {
				minecraft.getTextureManager().release(icon);
			}
		}

		public record Line(FormattedCharSequence text, int offset) {
			public Line(FormattedCharSequence text) {
				this(text, 0);
			}

			public static final Line EMPTY_LINE = new Line(FormattedCharSequence.EMPTY, 0);
		}
	}
}
