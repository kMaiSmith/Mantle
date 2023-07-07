package slimeknights.mantle.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.Mantle;

import static slimeknights.mantle.datagen.MantleTags.Fluids.LAVA;
import static slimeknights.mantle.datagen.MantleTags.Fluids.WATER;


/** Provider for tags added by mantle, generally not useful for other mods */
public class MantleFluidTagProvider extends FluidTagsProvider {
  public MantleFluidTagProvider(DataGenerator pGenerator, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
    super(pGenerator, Mantle.modId, existingFileHelper);
  }

  @Override
  protected void addTags() {
    this.tag(WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
    this.tag(LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
  }

  @Override
  public String getName() {
    return "Mantle Fluid Tag Provider";
  }
}
