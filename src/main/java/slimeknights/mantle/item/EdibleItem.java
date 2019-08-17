package slimeknights.mantle.item;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.mantle.common.IGeneratedJson;

import javax.annotation.Nullable;
import java.util.List;

public class EdibleItem extends Item implements IGeneratedJson {

  private boolean displayEffectsTooltip; // set to false to not display effects of food in tooltip

  public EdibleItem(Food foodIn, ItemGroup itemGroup) {
    this(foodIn, itemGroup, true);
  }

  public EdibleItem(Food foodIn, ItemGroup itemGroup, boolean displayEffectsTooltip) {
    super(new Properties().food(foodIn).group(itemGroup));
    this.displayEffectsTooltip = displayEffectsTooltip;
  }

  @Override
  public String getParentToUse() {
    return "item/generated";
  }

  @Override
  public JsonObject getTexturesToUse() {
    JsonObject textures = new JsonObject();

    textures.addProperty("layer0", this.getRegistryName().getNamespace() + ":item/" + this.getRegistryName().getPath() + " CHANGEME");

    return textures;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    TooltipItem.addOptionalTooltip(stack, tooltip);

    if (this.displayEffectsTooltip) {
      for (Pair<EffectInstance, Float> pair : stack.getItem().getFood().getEffects()) {
        if (pair.getLeft() != null) {
          tooltip.add(new StringTextComponent(TextFormatting.GRAY.toString() + I18n.format(pair.getLeft().getEffectName()).trim()));
        }
      }
    }

    super.addInformation(stack, worldIn, tooltip, flagIn);
  }
}
