package moriyashiine.bewitchment.client.integration.rei.category;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.recipe.Curse;
import moriyashiine.bewitchment.common.registry.BWObjects;
import moriyashiine.bewitchment.common.registry.BWRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CurseCategory implements RecipeCategory<CurseCategory.Display> {
	public static final Identifier IDENTIFIER = new Identifier(Bewitchment.MODID, "curses");
	public static final EntryStack LOGO = EntryStack.create(BWObjects.BRAZIER);
	
	@Override
	public @NotNull Identifier getIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public @NotNull String getCategoryName() {
		return I18n.translate("rei." + IDENTIFIER.toString().replaceAll(":", "."));
	}
	
	@Override
	public @NotNull EntryStack getLogo() {
		return LOGO;
	}
	
	@Override
	public int getDisplayHeight() {
		return 49;
	}
	
	@Override
	public @NotNull List<Widget> setupDisplay(Display recipeDisplay, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - 64, bounds.getCenterY() - 16);
		Point outputPoint = new Point(startPoint.x + 90, startPoint.y + 8);
		List<Widget> widgets = new ArrayList<>();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createArrow(new Point(startPoint.x + 53, startPoint.y + 8)));
		List<List<EntryStack>> inputs = recipeDisplay.getInputEntries();
		widgets.add(Widgets.createSlot(new Point(startPoint.x + 10, startPoint.y)).entries(inputs.get(0)).markInput());
		if (inputs.size() > 1) {
			widgets.add(Widgets.createSlot(new Point(startPoint.x + 28, startPoint.y)).entries(inputs.get(1)).markInput());
			if (inputs.size() > 2) {
				widgets.add(Widgets.createSlot(new Point(startPoint.x + 10, startPoint.y + 18)).entries(inputs.get(2)).markInput());
				if (inputs.size() > 3) {
					widgets.add(Widgets.createSlot(new Point(startPoint.x + 28, startPoint.y + 18)).entries(inputs.get(3)).markInput());
				}
			}
		}
		widgets.add(Widgets.createResultSlotBackground(outputPoint));
		widgets.add(Widgets.createSlot(outputPoint).entries(recipeDisplay.getResultingEntries().get(0)).disableBackground().markOutput());
		return widgets;
	}
	
	@SuppressWarnings("ConstantConditions")
	public static class Display implements RecipeDisplay {
		private final List<List<EntryStack>> input;
		private final List<List<EntryStack>> output;
		
		public Display(Curse recipe) {
			List<List<EntryStack>> input = new ArrayList<>();
			for (Ingredient ingredient : recipe.input) {
				List<EntryStack> entries = new ArrayList<>();
				for (ItemStack stack : ingredient.getMatchingStacksClient()) {
					entries.add(EntryStack.create(stack));
				}
				input.add(entries);
			}
			this.input = input;
			ItemStack brazier = new ItemStack(BWObjects.BRAZIER).setCustomName(new TranslatableText("curse." + BWRegistries.CURSES.getId(recipe.curse).toString().replace(":", ".")));
			brazier.getOrCreateTag().putInt("Cost", recipe.cost);
			output = Collections.singletonList(Collections.singletonList(EntryStack.create(brazier)));
		}
		
		@Override
		public @NotNull List<List<EntryStack>> getInputEntries() {
			return input;
		}
		
		@Override
		public @NotNull List<List<EntryStack>> getResultingEntries() {
			return output;
		}
		
		@Override
		public @NotNull Identifier getRecipeCategory() {
			return IDENTIFIER;
		}
	}
}
