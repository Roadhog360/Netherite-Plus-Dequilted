package com.oroarmor.rei_fishing_plugin.categories;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.oroarmor.netherite_plus.item.NetheritePlusItems;
import com.oroarmor.rei_fishing_plugin.display.LavaFishingDisplay;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.EntryStack.Settings;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import me.shedaniel.rei.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LavaFishingCategory implements RecipeCategory<LavaFishingDisplay> {

	@Override
	public ResourceLocation getIdentifier() {
		return new ResourceLocation("netherite_plus:lava_fishing_category");
	}

	@Override
	public EntryStack getLogo() {
		return EntryStack.create(NetheritePlusItems.NETHERITE_FISHING_ROD.get());
	}

	@Override
	public String getCategoryName() {
		return "Lava Fishing";
	}

	@Override
	public RecipeEntry getSimpleRenderer(LavaFishingDisplay recipe) {
		String name = getCategoryName();
		return new RecipeEntry() {
			@Override
			public int getHeight() {
				return 10 + Minecraft.getInstance().font.lineHeight;
			}

			@Override
			public void render(PoseStack matrices, Rectangle rectangle, int mouseX, int mouseY, float delta) {
				Minecraft.getInstance().font.draw(matrices, name, rectangle.x + 5, rectangle.y + 6, -1);
			}
		};
	}

	@Override
	public List<Widget> setupDisplay(LavaFishingDisplay display, Rectangle bounds) {
		List<Widget> widgets = Lists.newArrayList();
		widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 8, bounds.y + 3)).entry(getLogo().addSetting(Settings.TOOLTIP_APPEND_EXTRA, (stack) -> ImmutableList.of(new TextComponent(display.getOutcomes().getType().toString())))));
		Rectangle rectangle = new Rectangle(bounds.getCenterX() - bounds.width / 2 - 1, bounds.y + 23, bounds.width + 2, bounds.height - 28);
		widgets.add(Widgets.createSlotBase(rectangle));
		widgets.add(new ScrollableSlotsWidget(rectangle, CollectionUtils.map(display.getEntries(), t -> Widgets.createSlot(new Point(0, 0)).disableBackground().entry(t))));
		return widgets;
	}

	@Override
	public int getDisplayHeight() {
		return 140;
	}

	@Override
	public int getFixedRecipesPerPage() {
		return 1;
	}

	private static class ScrollableSlotsWidget extends WidgetWithBounds {
		private final Rectangle bounds;
		private final List<Slot> widgets;
		private final ScrollingContainer scrolling = new ScrollingContainer() {
			@Override
			public Rectangle getBounds() {
				Rectangle bounds = ScrollableSlotsWidget.this.getBounds();
				return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
			}

			@Override
			public int getMaxScrollHeight() {
				return Mth.ceil(widgets.size() / 8f) * 18;
			}
		};

		public ScrollableSlotsWidget(Rectangle bounds, List<Slot> widgets) {
			this.bounds = Objects.requireNonNull(bounds);
			this.widgets = Lists.newArrayList(widgets);
		}

		@Override
		public boolean mouseScrolled(double double_1, double double_2, double double_3) {
			if (containsMouse(double_1, double_2)) {
				scrolling.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
				return true;
			}
			return false;
		}

		@NotNull
		@Override
		public Rectangle getBounds() {
			return bounds;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (scrolling.updateDraggingState(mouseX, mouseY, button)) {
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
				return true;
			}
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
			scrolling.updatePosition(delta);
			Rectangle innerBounds = scrolling.getScissorBounds();
			ScissorsHandler.INSTANCE.scissor(innerBounds);
			for (int y = 0; y < Mth.ceil(widgets.size() / 8f); y++) {
				for (int x = 0; x < 8; x++) {
					int index = y * 8 + x;
					if (widgets.size() <= index) {
						break;
					}
					Slot widget = widgets.get(index);
					widget.getBounds().setLocation(bounds.x + 1 + x * 18, (int) (bounds.y + 1 + y * 18 - scrolling.scrollAmount));
					widget.render(matrices, mouseX, mouseY, delta);
				}
			}
			ScissorsHandler.INSTANCE.removeLastScissor();
			ScissorsHandler.INSTANCE.scissor(scrolling.getBounds());
			scrolling.renderScrollBar(0xff000000, 1, REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
			ScissorsHandler.INSTANCE.removeLastScissor();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return widgets;
		}
	}

}
